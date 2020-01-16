package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.*;

/**
 * @author OuYang Liang
 * @since 2020-01-15
 */
public class DefaultInstanceListener implements InstanceListener {

    @Override
    public void onChange() throws InterruptedException, KeeperException {
        List<String> workerList = ZkUtil.getInstance().getChildren(JupiterConfiguration.instance().getWorkerNode(), (event) -> {
            if (event.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) {
                try {
                    this.onChange();
                } catch (Exception e) {
                    // log.error(e.getMessage(), e);
                }
            }
        });
        List<Holder> holders = new LinkedList<>();
        Set<Integer> assigned = new HashSet<>();
        for (String worker : workerList) {
            Holder holder = new Holder();
            String content = ZkUtil.getInstance().getContent(JupiterConfiguration.instance().getWorkerNode() + JupiterConfiguration.SEPARATOR + worker, null);
            holder.node = worker;
            holder.setAssigned(content);
            holders.add(holder);
            assigned.addAll(holder.assigned);
        }

        JupiterConfiguration.instance().getTables().forEach((t) -> {
            if (!assigned.contains(t)) {
                holders.sort(Comparator.comparing(Holder::payload));
                holders.get(0).addAssigned(t);
            }
        });

        int lastIdx = holders.size() - 1;
        while (true) {
            holders.sort(Comparator.comparing(Holder::payload));

            if (holders.get(lastIdx).assigned.size() - holders.get(0).assigned.size() >= 2) {
                Integer tmp = holders.get(lastIdx).removeFirstAssigned();
                holders.get(0).addAssigned(tmp);

                continue;
            }

            break;
        }

        for (Holder holder : holders) {
            if (holder.affected) {
                try{
                    ZkUtil.getInstance().setContent(JupiterConfiguration.instance().getWorkerNode() + JupiterConfiguration.SEPARATOR + holder.node,
                            holder.assignedString());
                } catch(KeeperException e){
                    if (e instanceof KeeperException.NoNodeException) {
                        // 可能发生NONODE异常，这不是问题。
                        // NONODE意味着某个Worker下线了，Master会收到通知，并重新进行分配。
                        return;
                    }
                    throw e;
                }
            }
        }
    }

    private static class Holder {
        private String node;
        private List<Integer> assigned = new LinkedList<>();
        private boolean affected = false;

        public void addAssigned(Integer i) {
            this.assigned.add(i);
            affected = true;
        }

        public int payload() {
            return this.assigned.size();
        }

        public Integer removeFirstAssigned() {
            affected = true;
            return this.assigned.remove(0);
        }

        public void setAssigned(String nodeContent) {
            if (null == nodeContent || nodeContent.trim().isEmpty()) {
                return;
            }

            String[] parts = nodeContent.trim().split(JupiterConfiguration.GROUP_SEPARATOR);
            for (String part : parts) {
                assigned.add(Integer.valueOf(part.trim()));
            }
        }

        public String assignedString() {
            StringBuilder sb = new StringBuilder();
            int size = assigned.size();

            for (int i = 0; i < size; i++) {
                sb.append(this.assigned.get(i));
                if (i < (size - 1)) {
                    sb.append(JupiterConfiguration.GROUP_SEPARATOR);
                }
            }
            return sb.toString();
        }
    }
}

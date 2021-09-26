package com.personal.oyl.event.jupiter;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author OuYang Liang
 */
public class InstanceListener {
    private static final Logger log = LoggerFactory.getLogger(InstanceListener.class);
    private final ZkInstance zkInstance;

    public InstanceListener(ZkInstance zkInstance) {
        this.zkInstance = zkInstance;
    }

    public void onChange() throws LibeventException {
        List<String> workerList = this.getWorkers();
        List<Holder> holders = new LinkedList<>();
        Set<Integer> assigned = new HashSet<>();

        for (String worker : workerList) {
            Holder holder = new Holder();
            String assignment = this.getAssignment(worker);
            holder.node = worker;
            holder.setAssigned(assignment);
            holders.add(holder);
            assigned.addAll(holder.assigned);
        }

        JupiterConfiguration.instance()
                .getTables().stream()
                .filter(t -> !assigned.contains(t))
                .forEach(t -> this.assign(t, holders));

        this.balance(holders);
        for (Holder holder : holders) {
            if (holder.affected) {
                this.save(holder);
            }
        }
    }

    private List<String> getWorkers() throws LibeventException {
        try {
            return zkInstance.getChildren(JupiterConfiguration.instance().getWorkerNode(), (event) -> {
                if (event.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) {
                    try {
                        this.onChange();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        } catch (KeeperException | InterruptedException e) {
            throw new LibeventException(e);
        }
    }

    private String getAssignment(String worker) throws LibeventException {
        try {
            return zkInstance.getContent(JupiterConfiguration.instance().getWorkerNode() + JupiterConfiguration.SEPARATOR + worker, null);
        } catch (KeeperException | InterruptedException e) {
            throw new LibeventException(e);
        }
    }

    private void assign(int tbIdx, List<Holder> holders) {
        holders.sort(Comparator.comparing(Holder::payload));
        holders.get(0).addAssigned(tbIdx);
    }

    private void save(Holder holder) throws LibeventException {
        try {
            zkInstance.setContent(JupiterConfiguration.instance().getWorkerNode() + JupiterConfiguration.SEPARATOR + holder.node,
                    holder.assignedString());
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NoNodeException) {
                // 可能发生NONODE异常，这不是问题。
                // NONODE意味着某个Worker下线了，Master会收到通知，并重新进行分配。
                return;
            }
            throw new LibeventException(e);
        } catch (InterruptedException e) {
            throw new LibeventException(e);
        }
    }

    private void balance(List<Holder> holders) {
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
    }

    private static class Holder {
        private String node;
        private final List<Integer> assigned = new LinkedList<>();
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

        public void setAssigned(String assignment) {
            if (null == assignment || assignment.trim().isEmpty()) {
                return;
            }

            String[] parts = assignment.trim().split(JupiterConfiguration.GROUP_SEPARATOR);
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

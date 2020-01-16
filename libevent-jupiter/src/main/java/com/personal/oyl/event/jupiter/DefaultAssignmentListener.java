package com.personal.oyl.event.jupiter;


/**
 * @author OuYang Liang
 */
public class DefaultAssignmentListener implements AssignmentListener {

    private EventTransportMgr manager;

    public DefaultAssignmentListener(EventTransportMgr manager) {
        this.manager = manager;
    }

    @Override
    public void onChange(String assignment) {
        if (null == assignment || assignment.trim().isEmpty()) {
            return;
        }

        manager.stopAll();
        String[] parts = assignment.split(JupiterConfiguration.GROUP_SEPARATOR);

        for (String part : parts) {
            manager.start(Integer.parseInt(part.trim()));
        }
    }
}

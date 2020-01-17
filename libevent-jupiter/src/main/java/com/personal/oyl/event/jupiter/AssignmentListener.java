package com.personal.oyl.event.jupiter;


/**
 * @author OuYang Liang
 */
public class AssignmentListener {

    private EventTransportMgr manager;

    public AssignmentListener(EventTransportMgr manager) {
        this.manager = manager;
    }

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

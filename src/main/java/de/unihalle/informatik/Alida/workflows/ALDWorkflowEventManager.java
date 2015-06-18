/**
 * 
 */
package de.unihalle.informatik.Alida.workflows;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.ALDWorkflowEventType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;

/**
 * @author posch
 *
 */
class ALDWorkflowEventManager implements Runnable{
	
	/**
	 * The event queue associated with the listener 
	 */
	protected final BlockingDeque<ALDWorkflowEvent> eventQueue; 
	
	/**
	 * The listener for which to handle events
	 */
	private final ALDWorkflowEventListener listener;
	
	/**
	 * if true the thread is asked to terminate its run method
	 */
	// TODO use interrupt of thread
	private boolean termiate;
	
	public ALDWorkflowEventManager(ALDWorkflowEventListener listener) {
		super();
		this.eventQueue = new LinkedBlockingDeque<ALDWorkflowEvent>();
		this.listener = listener;
		this.termiate = false;
	}

	@Override
	public void run() {
		ALDWorkflowEvent event = null;
		while ( true) {
			try {
				event = eventQueue.take();
				if ( termiate) {
					break;
				}
				
				if ( ALDWorkflow.debug >= 1) {
					System.out.println( "    ALDWorkflowEventManager::run got new event <" +
							event.getTimeStamp() + "> of type " +
							event.getEventType() + ": " + event.getEventMessage());	
				}
				
				listener.handleALDWorkflowEvent( event);
				
				if ( event.getEventType() == ALDWorkflowEventType.EXECUTION_FINISHED ||
						event.getEventType() == ALDWorkflowEventType.USER_INTERRUPT ||
						event.getEventType() == ALDWorkflowEventType.RUN_FAILURE ) {
					if ( ALDWorkflow.debug >=1) {
						System.out.println("ALDWorkflowEventManager::run terminated due to terminate or user interrupt or run failure event");
					}
					return;
				}			
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * @return the terminate
	 */
	protected boolean isTermiate() {
		return termiate;
	}

	/**
	 * @param termiate the terminate state to set
	 */
	protected void setTermiate(boolean termiate) {
		this.termiate = termiate;
	}

}

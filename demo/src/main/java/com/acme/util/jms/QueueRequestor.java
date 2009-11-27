package com.acme.util.jms;

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TemporaryQueue;

public class QueueRequestor {

   QueueSession session; // The queue session the queue belongs to.
   Queue queue; // The queue to perform the request/reply on.
   TemporaryQueue tempQueue;
   QueueSender sender;
   QueueReceiver receiver;

   /**
    * Constructor for the <CODE>QueueRequestor</CODE> class.
    * 
    * <P>
    * This implementation assumes the session parameter to be non-transacted,
    * with a delivery mode of either <CODE>AUTO_ACKNOWLEDGE</CODE> or
    * <CODE>DUPS_OK_ACKNOWLEDGE</CODE>.
    * 
    * @param session
    *           the <CODE>QueueSession</CODE> the queue belongs to
    * @param queue
    *           the queue to perform the request/reply call on
    * 
    * @exception JMSException
    *               if the JMS provider fails to create the
    *               <CODE>QueueRequestor</CODE> due to some internal error.
    * @exception InvalidDestinationException
    *               if an invalid queue is specified.
    */

   public QueueRequestor(QueueSession session, Queue queue) throws JMSException
   {
      this.session = session;
      this.queue = queue;
      tempQueue = session.createTemporaryQueue();
      sender = session.createSender(queue);
      receiver = session.createReceiver(tempQueue);
   }

   /**
    * Sends a request and waits for a reply. The temporary queue is used for the
    * <CODE>JMSReplyTo</CODE> destination, and only one reply per request is
    * expected.
    * 
    * @param message
    *           the message to send
    * 
    * @return the reply message
    * 
    * @exception JMSException
    *               if the JMS provider fails to complete the request due to
    *               some internal error.
    */

   public Message request(Message message) throws JMSException
   {
      return request(message, 0);
   }

   public Message request(Message message, int wait) throws JMSException
   {
      message.setJMSReplyTo(tempQueue);
      sender.send(message);
      return (receiver.receive(wait));
   }

   /**
    * Closes the <CODE>QueueRequestor</CODE> and its session.
    * 
    * <P>
    * Since a provider may allocate some resources on behalf of a
    * <CODE>QueueRequestor</CODE> outside the Java virtual machine, clients
    * should close them when they are not needed. Relying on garbage collection
    * to eventually reclaim these resources may not be timely enough.
    * 
    * <P>
    * Note that this method closes the <CODE>QueueSession</CODE> object passed
    * to the <CODE>QueueRequestor</CODE> constructor.
    * 
    * @exception JMSException
    *               if the JMS provider fails to close the
    *               <CODE>QueueRequestor</CODE> due to some internal error.
    */

   public void close() throws JMSException
   {

      // publisher and consumer created by constructor are implicitly closed.
      session.close();
      tempQueue.delete();
   }
 }
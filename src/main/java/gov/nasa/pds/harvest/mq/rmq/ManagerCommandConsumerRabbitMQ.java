package gov.nasa.pds.harvest.mq.rmq;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import gov.nasa.pds.harvest.mq.ManagerCommandConsumer;
import gov.nasa.pds.registry.common.mq.msg.MQConstants;
import gov.nasa.pds.registry.common.mq.msg.ManagerMessage;


/**
 * A RabbitMQ consumer of manager messages
 * @author karpenko
 */
public class ManagerCommandConsumerRabbitMQ extends DefaultConsumer
{
    private Logger log;
    private Gson gson;
    
    private ManagerCommandConsumer mgrConsumer;

    
    /**
     * Constructor
     * @param channel RabbitMQ connection channel
     */
    public ManagerCommandConsumerRabbitMQ(Channel channel, ManagerCommandConsumer mgrConsumer)
    {
        super(channel);
        this.mgrConsumer = mgrConsumer;
        
        log = LogManager.getLogger(this.getClass());        
        gson = new Gson();
    }

    
    /**
     * Start consuming messages
     * @throws Exception
     */
    public void start() throws Exception
    {
        getChannel().basicConsume(MQConstants.MQ_MANAGER_COMMANDS, false, this);
    }

    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, 
            AMQP.BasicProperties properties, byte[] body) throws IOException
    {
        long deliveryTag = envelope.getDeliveryTag();
        ManagerMessage msg = null;
        
        try
        {
            String jsonStr = new String(body);
            msg = gson.fromJson(jsonStr, ManagerMessage.class);
        }
        catch(Exception ex)
        {
            log.error("Invalid message", ex);

            // ACK message (delete from the queue)
            getChannel().basicAck(deliveryTag, false);
            return;
        }
        
        if(mgrConsumer.processMessage(msg))
        {
            // ACK message (delete from the queue)
            getChannel().basicAck(deliveryTag, false);
        }
        else
        {
            // Reject and requeue
            getChannel().basicReject(deliveryTag, true);
        }
    }

}

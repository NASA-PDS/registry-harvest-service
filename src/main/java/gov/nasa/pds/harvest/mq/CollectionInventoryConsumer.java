package gov.nasa.pds.harvest.mq;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.harvest.dao.RegistryDao;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.registry.common.cfg.RegistryCfg;
import gov.nasa.pds.registry.common.es.service.CollectionInventoryWriter;
import gov.nasa.pds.registry.common.mq.msg.CollectionInventoryMessage;
import gov.nasa.pds.registry.common.util.ExceptionUtils;

/**
 * Consume collection inventory messages
 * @author karpenko
 */
public class CollectionInventoryConsumer
{
    protected Logger log;
    private CollectionInventoryWriter proc;
    
    
    /**
     * Constructor
     * @param cfg registry (Elasticsearch) configuration
     * @throws Exception an exception
     */
    public CollectionInventoryConsumer(RegistryCfg cfg) throws Exception
    {
        log = LogManager.getLogger(this.getClass());
        proc = new CollectionInventoryWriter(cfg);
    }
    
    
    /**
     * Process collection inventory message
     * @param msg collection inventory message
     * @return true if message successfully processed
     */
    public boolean processMessage(CollectionInventoryMessage msg)
    {
        if(msg == null) return true;
        
        if(msg.collectionLidvid == null || msg.collectionLidvid.isBlank())
        {
            return true;
        }
        
        if(msg.inventoryFile == null || msg.inventoryFile.isBlank())
        {
            return true;
        }
        
        if(!msg.overwrite)
        {
            // Check if this collection already registered
            RegistryDao regDao = RegistryManager.getInstance().getRegistryDao();
            try
            {
                if(regDao.idExists(msg.collectionLidvid))
                {
                    return true;
                }
            }
            catch(Exception ex)
            {
                log.error("Could not determine if a collection '" + msg.collectionLidvid 
                        + "' already registered. Ignoring collection inventory. " + ExceptionUtils.getMessage(ex));
                return true;
            }
        }
        
        // Process collection inventory
        File inventoryFile = new File(msg.inventoryFile);
        if(!inventoryFile.exists())
        {
            log.error("Collection inventory file " + msg.inventoryFile + " doesn't exist.");
            return true;
        }
        
        log.info("Processing collection inventory file " + msg.inventoryFile);
        
        try
        {
            proc.writeCollectionInventory(msg.collectionLidvid, inventoryFile, msg.jobId);
            return true;
        }
        catch(Exception ex)
        {
            log.error(ExceptionUtils.getMessage(ex));
            return false;
        }
    }
}

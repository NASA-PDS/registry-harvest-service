package gov.nasa.pds;

import java.util.Arrays;
import java.util.Set;

import gov.nasa.pds.harvest.cfg.Configuration;
import gov.nasa.pds.harvest.cfg.RegistryCfg;
import gov.nasa.pds.harvest.dao.RegistryManager;
import gov.nasa.pds.harvest.dao.SchemaDao;
import gov.nasa.pds.harvest.meta.FieldNameCache;
import gov.nasa.pds.harvest.mq.msg.ProductMessage;
import gov.nasa.pds.harvest.mq.rmq.ProductConsumer;
import gov.nasa.pds.harvest.util.Log4jConfigurator;


public class TestProductConsumer
{

    public static void main(String[] args) throws Exception
    {
        try
        {
            Configuration cfg = createConfiguration();
            Log4jConfigurator.configure("INFO", "/tmp/t.log");
            initRegistry(cfg.registryCfg);
            
            ProductConsumer consumer = new ProductConsumer(cfg.harvestCfg, cfg.registryCfg);
            
            ProductMessage msg = createTestMessage();
            consumer.processMessage(msg);
        }
        finally
        {
            RegistryManager.destroy();
        }
    }

    
    private static void initRegistry(RegistryCfg cfg) throws Exception
    {
        RegistryManager.init(cfg);

        SchemaDao schemaDao = RegistryManager.getInstance().getSchemaDAO();
        Set<String> fields = schemaDao.getFieldNames();
        FieldNameCache.getInstance().set(fields);
    }
    
    
    private static Configuration createConfiguration()
    {
        Configuration cfg = new Configuration();
        
        // Harvest
        cfg.harvestCfg.processDataFiles = false;
        
        // Registry
        cfg.registryCfg.url = "http://localhost:9200";
        cfg.registryCfg.indexName = "t1";
        
        return cfg;
    }
    
    
    private static ProductMessage createTestMessage()
    {
        ProductMessage msg = new ProductMessage();
        msg.jobId = "TestJob123";
        msg.nodeName = "TestNode";
        msg.overwrite = true;
        msg.files = Arrays.asList("/tmp/d5/orex.xml");
        msg.lidvids = Arrays.asList("lidvid:test:1234::1.0");
        
        return msg;
    }
}
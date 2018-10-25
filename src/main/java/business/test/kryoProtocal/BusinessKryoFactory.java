package business.test.kryoProtocal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

public class BusinessKryoFactory{

    private KryoPool pool;
    public BusinessKryoFactory(){
            KryoFactory factory = new KryoFactory() {
                public Kryo create() {
                    Kryo kryo = new Kryo();
                    return kryo;
                }
            };
            // Simple pool, you might also activate SoftReferences to fight OOMEs.
            pool = new KryoPool.Builder(factory).build();
    }

    public KryoPool getPool() {
        return pool;
    }
}

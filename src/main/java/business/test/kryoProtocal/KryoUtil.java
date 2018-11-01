package business.test.kryoProtocal;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.Input;

import java.io.ByteArrayOutputStream;

public class KryoUtil{

    private static BusinessKryoFactory factory = new BusinessKryoFactory();

    public static byte[]  doSerializable(Object obj){
        Kryo kryo = null;
        try{
            kryo  = factory.getPool().borrow();

            kryo.setReferences(false);
            //kryo.register(c);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Output output = new Output(baos);
            kryo.writeClassAndObject(output, obj);
            output.flush();
            output.close();

            baos.flush();
            baos.close();


            byte[] b = baos.toByteArray();
            return b;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(kryo != null){
                factory.getPool().release(kryo);
            }
        }
        return null;
    }

    public static Object parseSerializable(byte[] b){
        Kryo kryo = null;
        try{
            kryo  = factory.getPool().borrow();
            kryo.setReferences(false);
            //kryo.register(c);

            Input input = new Input(b);

            Object obj = kryo.readClassAndObject(input);

            input.read(b);
            input.close();
            return obj;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(kryo != null){
                factory.getPool().release(kryo);
            }
        }
        return null;
    }
}

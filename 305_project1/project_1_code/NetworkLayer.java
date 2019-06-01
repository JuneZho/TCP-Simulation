import java.util.concurrent.TimeUnit;

public class NetworkLayer
{

    private LinkLayer linkLayer;
    private double transmission_delay;
    private long propogation_delay;
    private boolean experiment;

    public NetworkLayer(boolean server){
        linkLayer = new LinkLayer(server);
        this.transmission_delay = 0;
        this.propogation_delay = 0;
        experiment = false;
    }

    public void set_experiment(){
        experiment = true;
    }

    public void set_transmission_delay(double transmission_delay){
        this.transmission_delay = transmission_delay;
    }

    public void set_propogation_delay(long propogation_delay){
        this.propogation_delay = propogation_delay;
    }

    public NetworkLayer(boolean server, double transmission_delay, long propogation_delay)
    {
        linkLayer = new LinkLayer(server);
        this.transmission_delay = transmission_delay;
        this.propogation_delay = propogation_delay;
    }

    public double send(byte[] payload)
    {
        double time = 0.0; 
        try{
            long x = payload.length;
            double delay = x * transmission_delay;
            //System.out.println("Transmission delay for " + delay + " seconds");
            if(!experiment){
                TimeUnit.SECONDS.sleep((long)delay);
            }
            time += delay;
        }catch(Exception e){
            System.out.println("fail to add transmission delay");
        }

        try{
            //System.out.println("Propogation delay for " +propogation_delay + " seconds");
            if(!experiment){
                TimeUnit.SECONDS.sleep(propogation_delay);
            }
            time += propogation_delay;
        }catch(Exception e){
            System.out.println("fail to add propogation delay");
        }

        linkLayer.send( payload );
        return time;
    }

    public byte[] receive()
    {
        byte[] payload = linkLayer.receive();
        return payload;
    }
}

package world.creve.playpit.util;
import java.util.Random;
/** Stable feather positions. A seed encodes an immutable growth band and event-local ordinal.
* Later posts/visibility changes do not move existing petals. Radical-inverse placement is
* injective within a band; disjoint bands prevent cross-band complete overlap.
*/
public final class PetalLayout {
    private PetalLayout() {
    }
    public record Position(double x,double y,double rotation,double scale) {
    }
    public static int growth(long count) {
        if(count<0)throw new IllegalArgumentException("Negative count");
        return count==0?0:count<=10?1:count<=30?2:count<=60?3:4;
    }
    public static long seed(long ordinal,int stage) {
        if(ordinal<1||ordinal>(1L<<45)||stage<1||stage>4)throw new IllegalArgumentException("Layout capacity exceeded");
        return (ordinal<<3)|stage;
    }
    public static int type(long seed) {
        return Math.floorMod(seed,5)+1;
    }
    private static double inverse(long n,int base) {
        double value=0,f=1.0/base;
        while(n>0) {
            value+=(n%base)*f;
            n/=base;
            f/=base;
        }
        return value;
    }
    public static Position position(long seed) {
        long index=seed>>>3;
        int band=(int)(seed&7);
        if(index<1||band<1||band>4)throw new IllegalArgumentException("Invalid petal seed");
        double[] edges= {
            0,.10,.32,.58,.80,1.0
        }
        ;
        double u=edges[band]+(edges[band+1]-edges[band])*inverse(index,2);
        double v=2*inverse(index,3)-1;
        double w=3+17*Math.sin(Math.PI*u);
        // Invertible (u,v) -> feather plane: x+y determines u uniquely.
        double x=10+76*u+v*w;
        double y=82-57*u-v*w;
        Random random=new Random(seed);
        return new Position(x,y,-38+v*23,0.65+random.nextDouble()*.25);
    }
}

package james.asteroid.data.drawer;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import james.asteroid.data.DrawerData;
import james.asteroid.data.ParticleData;

public class BackgroundDrawer extends DrawerData {

    private List<ParticleData> particles;

    public BackgroundDrawer(Paint paint) {
        super(paint);
        particles = new ArrayList<>();
        particles.add(new ParticleData(paint(0)));
    }

    public void addParticle(ParticleData particle) {
        particles.add(particle);
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        for (ParticleData particle : new ArrayList<>(particles)) {
            if (!particle.draw(canvas, speed))
                particles.remove(particle);
        }

        particles.add(new ParticleData(paint(0)));
        return true;
    }
}

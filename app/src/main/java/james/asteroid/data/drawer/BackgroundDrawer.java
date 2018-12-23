package james.asteroid.data.drawer;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import james.asteroid.data.CloudData;
import james.asteroid.data.DrawerData;
import james.asteroid.data.ParticleData;

public class BackgroundDrawer extends DrawerData {

    private List<ParticleData> particles;
    private List<CloudData> clouds;
    private int frame;

    public BackgroundDrawer(Paint paint, Paint cloudPaint) {
        super(paint, cloudPaint);
        particles = new ArrayList<>();
        particles.add(new ParticleData(paint(0)));
        clouds = new ArrayList<>();
        clouds.add(new CloudData(cloudPaint, 0, 0));
        clouds.add(new CloudData(cloudPaint, 1, 1));
    }

    /**
     * Add a particle to the drawer.
     *
     * @param particle          The particle to add.
     */
    public void addParticle(ParticleData particle) {
        particles.add(particle);
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        for (CloudData cloud : new ArrayList<>(clouds)) {
            if (!cloud.draw(canvas, speed))
                clouds.remove(cloud);
        }

        for (ParticleData particle : new ArrayList<>(particles)) {
            if (!particle.draw(canvas, speed))
                particles.remove(particle);
        }

        particles.add(new ParticleData(paint(0)));

        frame++;
        if (frame % 10 == 0) {
            frame = 0;

            clouds.add(new CloudData(paint(1),
                    (float) Math.max(0, Math.min(canvas.getWidth() - 1, clouds.get(clouds.size() - 2).getStart() + (Math.random() * 0.2) - 0.1)),
                    (float) Math.max(0, Math.min(canvas.getWidth() - 1, clouds.get(clouds.size() - 2).getEnd() + (Math.random() * 0.2) - 0.1))));

            clouds.add(new CloudData(paint(1),
                    (float) Math.max(0, Math.min(canvas.getWidth() - 1, clouds.get(clouds.size() - 2).getStart() + (Math.random() * 0.2) - 0.1)),
                    (float) Math.max(0, Math.min(canvas.getWidth() - 1, clouds.get(clouds.size() - 2).getEnd() + (Math.random() * 0.2) - 0.1))));
        }

        return true;
    }
}

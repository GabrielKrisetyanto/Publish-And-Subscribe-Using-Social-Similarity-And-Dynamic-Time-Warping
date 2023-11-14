/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package movement;

import core.Coord;
import core.Settings;
import java.util.HashMap;
import java.util.Map;
import static ku.Helper.gridCoordinates;

/**
 *
 * @author User
 */
public class ClusterKu extends MovementModel {

    private static final int PATH_LENGTH = 1;
    private Coord lastWaypoint;
    private Double randX, randY;
    private static Map<Integer, double[]> AREA;

    static {
        AREA = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            double p = 1200 / 3;
            double[] a = gridCoordinates(i, p);
            AREA.put(i, a);
        }
    }

    public ClusterKu(Settings settings) {
        super(settings);
    }

    protected ClusterKu(ClusterKu rwp) {
        super(rwp);
    }

    @Override
    public Coord getInitialLocation() {
        assert rng != null : "MovementModel not initialized!";
        Coord c = randomCoord();

        this.lastWaypoint = c;
        return c;
    }

    @Override
    public Path getPath() {
        Path p;
        p = new Path(generateSpeed());
        p.addWaypoint(lastWaypoint.clone());
        Coord c = lastWaypoint;

        for (int i = 0; i < PATH_LENGTH; i++) {
            c = randomCoord();
            p.addWaypoint(c);
        }

        this.lastWaypoint = c;
        return p;
    }

    private void setXY(int moveType) {
        double[] coord = AREA.get(moveType);
        randX = coord[0] + (rng.nextDouble() * (coord[1] - coord[0]));
        randY = coord[2] + (rng.nextDouble() * (coord[3] - coord[2]));
        
    }

    protected Coord randomCoord() {
        switch (this.moveArea) {
            case 1:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 2:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 3:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 4:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 5:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 6:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 7:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 8:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 9:
                if (Math.random() >= 0.2) {
                    setXY(moveArea);
                    return new Coord(randX, randY);
                } else {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                }
            case 10:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(1);
                    return new Coord(randX, randY);
                }
            case 11:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(2);
                    return new Coord(randX, randY);
                }
            case 12:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(3);
                    return new Coord(randX, randY);
                }
            case 13:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(4);
                    return new Coord(randX, randY);
                }
            case 14:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(5);
                    return new Coord(randX, randY);
                }
            case 15:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(6);
                    return new Coord(randX, randY);
                }
            case 16:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(7);
                    return new Coord(randX, randY);
                }
            case 17:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(8);
                    return new Coord(randX, randY);
                }
            case 18:
                if (Math.random() >= 0.4) {
                    return new Coord(rng.nextDouble() * getMaxX(),
                            rng.nextDouble() * getMaxY());
                } else {
                    setXY(9);
                    return new Coord(randX, randY);
                }
            default:
                return new Coord(rng.nextDouble() * getMaxX(),
                        rng.nextDouble() * getMaxY());
        }
    }

    @Override
    public ClusterKu replicate() {
        return new ClusterKu(this);
    }
}

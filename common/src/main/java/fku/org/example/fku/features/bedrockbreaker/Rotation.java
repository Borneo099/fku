package fku.org.example.fku.features.bedrockbreaker;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public record Rotation(float xRot, float yRot) {
    public static Rotation findClosest(Rotation current, Rotation target, Direction direction) {
        if (target == null) {
            return null;
        }
        float step = 2.5f;
        float xRot = current.xRot();
        float yRot = current.yRot();
        while (Rotation.nearestDirection(xRot, yRot) != direction) {
            float deltaXRot = Rotation.getDeltaXRot(target.xRot, xRot);
            float deltaYRot = Rotation.getDeltaYRot(target.yRot, yRot);
            double len = Math.sqrt(deltaXRot * deltaXRot + deltaYRot * deltaYRot);
            double xRotFactor = Math.abs(deltaXRot) / len;
            double yRotFactor = Math.abs(deltaYRot) / len;
            xRot += ((Math.signum(deltaXRot) * 2.5f) * xRotFactor);
            yRot += ((Math.signum(deltaYRot) * 2.5f) * yRotFactor);
        }
        return new Rotation(xRot, yRot);
    }

    public static Direction nearestDirection(float xRotDegrees, float yRotDegrees) {
        Direction dirNS;
        float xRad = xRotDegrees * (float)(Math.PI / 180);
        float yRad = -yRotDegrees * (float)(Math.PI / 180);
        float sinX = Mth.sin(xRad);
        float cosX = Mth.cos(xRad);
        float sinY = Mth.sin(yRad);
        float cosY = Mth.cos(yRad);
        boolean east = sinY > 0.0f;
        boolean up = sinX < 0.0f;
        boolean south = cosY > 0.0f;
        float magEW = east ? sinY : -sinY;
        float magUD = up ? -sinX : sinX;
        float magNS = south ? cosY : -cosY;
        float projEW = magEW * cosX;
        float projNS = magNS * cosX;
        Direction dirEW = east ? Direction.EAST : Direction.WEST;
        Direction dirUD = up ? Direction.UP : Direction.DOWN;
        Direction direction = dirNS = south ? Direction.SOUTH : Direction.NORTH;
        if (magEW > magNS) {
            return magUD > projEW ? dirUD : dirEW;
        }
        return magUD > projNS ? dirUD : dirNS;
    }

    private static float getDeltaXRot(float xRot1, float xRot2) {
        return xRot1 - xRot2;
    }

    private static float getDeltaYRot(float yRot1, float yRot2) {
        float delta;
        if (Float.isNaN(yRot1) || Float.isNaN(yRot2)) {
            return 0.0f;
        }
        for (delta = yRot1 - yRot2; delta < -180.0f; delta += 360.0f) {
        }
        while (delta >= 180.0f) {
            delta -= 360.0f;
        }
        return delta;
    }
}


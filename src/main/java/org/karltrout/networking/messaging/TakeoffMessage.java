package org.karltrout.networking.messaging;

/**
 * Created by karltrout on 10/25/17.
 */
public class TakeoffMessage extends SimulationMessage {

    double latitude = 0.0, longitude = 0.0, speed = 0.0;
    float heading = 0.0f, yaw = 0.0f, pitch = 0.0f;

    public TakeoffMessage() {
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public float getHeading() {
        return heading;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public static class Builder{

        TakeoffMessage message;
        public Builder(){
            this.message = new TakeoffMessage();
        }

        public Builder withSpeed(double speed){ this.message.speed = speed; return this;}
        public Builder withLatitude(double latitude) { this.message.latitude = latitude; return this;}
        public Builder withLongitude(double longitude) { this.message.longitude = longitude; return this;}
        public Builder withHeading(float heading) { this.message.heading = heading; return this;}
        public Builder withYaw(float yaw) { this.message.yaw = yaw; return this;}
        public Builder withPitch(float pitch) { this.message.pitch = pitch; return this;}

        public TakeoffMessage build(){ return this.message;}


    }
}

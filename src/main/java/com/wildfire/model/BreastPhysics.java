package com.wildfire.model;

public class BreastPhysics {
    private float velocity;
    private float position;
    private float acceleration;
    private final float damping = 0.98f; // Damping factor to reduce oscillation

    public BreastPhysics() {
        this.velocity = 0.0f;
        this.position = 0.0f;
        this.acceleration = 0.0f;
    }

    public void applyForce(float force) {
        this.acceleration += force;
    }

    public void update() {
        this.velocity += this.acceleration;
        this.velocity *= damping; // Apply damping to reduce velocity
        this.position += this.velocity;
        this.acceleration = 0.0f; // Reset acceleration after applying force
    }

    public float getPosition() {
        return this.position;
    }
}

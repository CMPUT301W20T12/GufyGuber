package com.example.gufyguber;

public class RideRequest {
    enum Status {
        PENDING {
            @Override
            public String toString() {
                return "Pending";
            }
        },
        ACCEPTED {
            @Override
            public String toString() {
                return "Accepted";
            }
        },
        COMPLETED {
            @Override
            public String toString() {
                return "Completed";
            }
        }
    }

    //TODO: Reference to a Rider
    //TODO: Reference to a Driver

    private Status status;
    public Status getStatus(){ return status; }
    public void setStatus(Status status) { this.status = status; }

    private float offeredFare;
    public float getOfferedFare() { return offeredFare; }
    public void setOfferedFare(float offeredFare) { this.offeredFare = offeredFare; }

    //TODO: A locally owned LocationInfo instance
    //TODO: A locally owned TimeInfo instance

    //TODO: Ride request constructor that has rider, location, and fare info parameters

    public void cancelRideRequest() {
        //TODO: Handle canceling and cleaning up after a ride request
    }
}

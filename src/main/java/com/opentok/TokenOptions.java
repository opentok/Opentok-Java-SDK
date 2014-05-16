package com.opentok;

import com.opentok.exception.InvalidArgumentException;

public class TokenOptions {

    private Role role;
    private double expireTime;
    private String data;

    private TokenOptions(Builder builder) {
        this.role = builder.role != null ? builder.role : Role.PUBLISHER;

        // default value calculated at token generation time
        this.expireTime = builder.expireTime;

        // default value of null means to omit the key "connection_data" from the token
        this.data = builder.data;
    }

    public Role getRole() {
        return role;
    }

    public double getExpireTime() {
        return expireTime;
    }

    public String getData() {
        return data;
    }

    public static class Builder {
        private Role role;
        private double expireTime = 0;
        private String data;

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder expireTime(double expireTime) {
            // NOTE: since this object can be stored/cached, validation should occur at token generation time
            this.expireTime = expireTime;
            return this;
        }

        public Builder data(String data) throws InvalidArgumentException {
            if (data.length() <= 1000) {
                this.data = data;
            } else {
                throw new InvalidArgumentException(
                        "The given connection data is too long, limit is 1000 characters: " + data.length());
            }
            return this;
        }

        public TokenOptions build() {
            return new TokenOptions(this);
        }
    }

}

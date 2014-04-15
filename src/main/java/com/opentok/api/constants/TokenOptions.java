package com.opentok.api.constants;

import com.opentok.exception.OpenTokInvalidArgumentException;

/**
 * Created by ankur on 4/14/14.
 */
public class TokenOptions {

    private String role;
    private double expireTime;
    private String data;

    private TokenOptions(Builder builder) {
        this.role = builder.role != null ? builder.role : "publisher";

        // default value calculated at token generation time
        this.expireTime = builder.expireTime;

        // default value of null means to omit the key "connection_data" from the token
        this.data = builder.data;
    }

    public String getRole() {
        return role;
    }

    public double getExpireTime() {
        return expireTime;
    }

    public String getData() {
        return data;
    }

    public static class Builder {
        private String role;
        private double expireTime = 0;
        private String data;

        public Builder role(String role) throws OpenTokInvalidArgumentException {
            if (role.equals("publisher") || role.equals("subscriber") || role.equals("moderator")) {
                this.role = role;
            } else {
                throw new OpenTokInvalidArgumentException("The given role is not valid: " + role);
            }
            return this;
        }

        public Builder expireTime(double expireTime) {
            // NOTE: since this object can be stored/cached, validation should occur at token generation time
            this.expireTime = expireTime;
            return this;
        }

        public Builder data(String data) throws OpenTokInvalidArgumentException {
            if (data.length() <= 1000) {
                this.data = data;
            } else {
                throw new OpenTokInvalidArgumentException(
                        "The given connection data is too long, limit is 1000 characters: "+data.length());
            }
            return this;
        }

        public TokenOptions build() {
            return new TokenOptions(this);
        }
    }

}

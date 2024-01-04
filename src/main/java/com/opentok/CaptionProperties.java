/**
 * OpenTok Java SDK
 * Copyright (C) 2024 Vonage.
 * http://www.tokbox.com
 * <p>
 * Licensed under The MIT License (MIT). See LICENSE file for more information.
 */
package com.opentok;

/**
 * Defines values for the <code>properties</code> parameter of the
 * {@link OpenTok#startCaptions(String, String, CaptionProperties)} method.
 *
 * @see OpenTok#startCaptions(String, String, CaptionProperties)
 */
public class CaptionProperties {
    private final String statusCallbackUrl, languageCode;
    private final int maxDuration;
    private final boolean partialCaptions;

    private CaptionProperties(Builder builder) {
        statusCallbackUrl = builder.statusCallbackUrl;
        languageCode = builder.languageCode;
        maxDuration = builder.maxDuration;
        partialCaptions = builder.partialCaptions;
    }

    /**
     * A publicly reachable URL controlled by the customer and capable of generating the content to
     * be rendered without user intervention. The minimum length of the URL is 15 characters and the
     * maximum length is 2048 characters. For more information, see
     * <a href=https://tokbox.com/developer/guides/live-captions/#live-caption-status-updates>
     * Live Caption status updates</a>.
     *
     * @return The status callback URL as a string, or {@code null} if not set.
     */
    public String getStatusCallbackUrl() {
        return statusCallbackUrl;
    }

    /**
     * The BCP-47 code for a spoken language used on this call.
     *
     * @return The language code as a string.
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * The maximum duration for the audio captioning, in seconds.
     *
     * @return The maximum captioning duration as an integer.
     */
    public int getMaxDuration() {
        return maxDuration;
    }

    /**
     * Whether faster captioning is enabled at the cost of some degree of inaccuracies.
     *
     * @return {@code true} if the partial captions setting is enabled.
     */
    public boolean partialCaptions() {
        return partialCaptions;
    }

    /**
     * Entry point for constructing an instance of this class.
     *
     * @return A new Builder.
     */
    public static Builder Builder() {
        return new Builder();
    }

    /**
     * Used to create a CaptionProperties object.
     *
     * @see CaptionProperties
     */
    public static class Builder {
        private String languageCode = "en-US", statusCallbackUrl;
        private int maxDuration = 14400;
        private boolean partialCaptions = true;

        private Builder() {
        }

        /**
         * The BCP-47 code for a spoken language used on this call. The default value is "en-US". The following
         * language codes are supported: "en-AU" (English, Australia), "en-GB" (Englsh, UK), "es-US" (English, US),
         * "zh-CN" (Chinese, Simplified), "fr-FR" (French), "fr-CA" (French, Canadian), "de-DE" (German),
         * "hi-IN" (Hindi, Indian), "it-IT" (Italian), "ja-JP" (Japanese), "ko-KR" (Korean),
         * "pt-BR" (Portuguese, Brazilian), "th-TH" (Thai).
         *
         * @param languageCode The BCP-47 language code as a string.
         *
         * @return This Builder with the languageCode property setting.
         */
        public Builder languageCode(String languageCode) {
            if (languageCode == null || languageCode.length() != 5 || languageCode.charAt(2) != '-') {
                throw new IllegalArgumentException("Invalid language code.");
            }
            this.languageCode = languageCode;
            return this;
        }

        /**
         * A publicly reachable URL controlled by the customer and capable of generating the content to
         * be rendered without user intervention. The minimum length of the URL is 15 characters and the
         * maximum length is 2048 characters. For more information, see
         * <a href=https://tokbox.com/developer/guides/live-captions/#live-caption-status-updates>
         * Live Caption status updates</a>.
         *
         * @param statusCallbackUrl The status callback URL as a string.
         *
         * @return This Builder with the statusCallbackUrl property setting.
         */
        public Builder statusCallbackUrl(String statusCallbackUrl) {
            if (statusCallbackUrl == null || statusCallbackUrl.length() < 15 || statusCallbackUrl.length() > 2048) {
                throw new IllegalArgumentException("Status callback URL must be between 15 and 2048 characters.");
            }
            this.statusCallbackUrl = statusCallbackUrl;
            return this;
        }

        /**
         * The maximum duration for the audio captioning, in seconds.
         * The default value is 14,400 seconds (4 hours), the maximum duration allowed.
         *
         * @param maxDuration The maximum captions duration in seconds.
         *
         * @return This Builder with the maxDuration property setting.
         */
        public Builder maxDuration(int maxDuration) {
            if ((this.maxDuration = maxDuration) < 0 || maxDuration > 14400) {
                throw new IllegalArgumentException("Max duration must be positive and less than 14400 seconds.");
            }
            return this;
        }

        /**
         * Whether to enable this to faster captioning at the cost of some degree of inaccuracies.
         * The default value is {@code true}.
         *
         * @param partialCaptions Whether to enable faster captions.
         *
         * @return This Builder with the partialCaptions property setting.
         */
        public Builder partialCaptions(boolean partialCaptions) {
            this.partialCaptions = partialCaptions;
            return this;
        }

        /**
         * Builds the CaptionProperties object.
         *
         * @return The CaptionProperties object with this builder's settings.
         */
        public CaptionProperties build() {
            return new CaptionProperties(this);
        }
    }
}

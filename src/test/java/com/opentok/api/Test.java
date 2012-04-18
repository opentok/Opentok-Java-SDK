package com.tokbox.api;

import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.OpenTokSession;
import com.opentok.api.constants.SessionProperties;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;
import com.opentok.util.TokBoxXML;
import java.util.*;
import java.net.*;
import java.io.*;

class UnitTest {
    OpenTokSDK sdk;

    public UnitTest() {
		sdk = new OpenTokSDK(API_Config.API_KEY,API_Config.API_SECRET);
    }

    private TokBoxXML get_session_info(String session_id) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-TB-TOKEN-AUTH", "devtoken");
		TokBoxXML xml;
		try {
            xml = new TokBoxXML(request(API_Config.API_URL + "/session/" + session_id + "?extended=true", new HashMap<String, String>(), headers));
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    return null;
        }
		return xml;
    }

    private TokBoxXML get_token_info(String token) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-TB-TOKEN-AUTH",token);
		TokBoxXML xml;
		try {
            xml = new TokBoxXML(request(API_Config.API_URL + "/token/validate", new HashMap<String, String>(), headers));
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    return null;
        }
		return xml;
    }

	public boolean test_create_sesion() {
		try {
		    // No params
            OpenTokSession session = sdk.create_session();
            TokBoxXML xml = get_session_info(session.session_id);
            if(!xml.getElementValue("session_id", "Session").equals(session.session_id)) {
                System.out.println("Java SDK tests: Simple session create failed");
                return false;
            }

		    // location
            session = sdk.create_session("216.38.134.114");
            xml = get_session_info(session.session_id);
            if(!xml.getElementValue("session_id", "Session").equals(session.session_id)) {
                System.out.println("Java SDK tests: Session create with location failed");
                return false;
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: create_sesion failed");
		    return false;
        }

        return true;
    }
    public boolean test_num_output_streams() {
		try {
		    // 0
		    SessionProperties sp = new SessionProperties();
		    sp.multiplexer_numOutputStreams = 0;
            OpenTokSession s = sdk.create_session("216.38.134.114", sp);
            TokBoxXML xml = get_session_info(s.session_id);
            if(!xml.getElementValue("numOutputStreams", "multiplexer").equals("0")) {
                System.out.println("Java SDK tests: num output streams not set to 0");
                return false;
            }
		    // 1
		    sp = new SessionProperties();
		    sp.multiplexer_numOutputStreams = 1;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("numOutputStreams", "multiplexer").equals("1")) {
                System.out.println("Java SDK tests: num output streams not set to 1");
                return false;
            }
		    // 5
		    sp = new SessionProperties();
		    sp.multiplexer_numOutputStreams = 5;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("numOutputStreams", "multiplexer").equals("5")) {
                System.out.println("Java SDK tests: num output streams not set to 5");
                return false;
            }
		    // 100
		    sp = new SessionProperties();
		    sp.multiplexer_numOutputStreams = 100;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("numOutputStreams", "multiplexer").equals("100")) {
                System.out.println("Java SDK tests: num output streams not set to 100");
                return false;
            }
        } catch (OpenTokException e) {
            System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: num output streams failed");
		    return false;
        }
        return true;
    }

    public boolean test_switch_type() {
		try {
		    // 0
		    SessionProperties sp = new SessionProperties();
		    sp.multiplexer_switchType = 0;
            OpenTokSession s = sdk.create_session("216.38.134.114", sp);
            TokBoxXML xml = get_session_info(s.session_id);
            if(!xml.getElementValue("switchType", "multiplexer").equals("0")) {
                System.out.println("Java SDK tests: switch type not set to 0");
                return false;
            }
            // 1
		    sp = new SessionProperties();
		    sp.multiplexer_switchType = 1;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("switchType", "multiplexer").equals("1")) {
                System.out.println("Java SDK tests: switch type not set to 1");
                return false;
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: switch type failed");
		    return false;
        }
        return true;
    }

    public boolean test_switch_timeout() {
		try {
		    // < 2000
		    SessionProperties sp = new SessionProperties();
		    sp.multiplexer_switchTimeout = 435;
            OpenTokSession s = sdk.create_session("216.38.134.114", sp);
            TokBoxXML xml = get_session_info(s.session_id);
            if(!xml.getElementValue("switchTimeout", "multiplexer").equals("435")) {
                System.out.println("Java SDK tests: switch timeout not set to 435");
                return false;
            }
		    // =2000
		    sp = new SessionProperties();
		    sp.multiplexer_switchTimeout = 2000;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("switchTimeout", "multiplexer").equals("2000")) {
                System.out.println("Java SDK tests: switch timeout not set to 2000");
                return false;
            }
		    // > 2000
		    sp = new SessionProperties();
		    sp.multiplexer_switchTimeout = 4350;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("switchTimeout", "multiplexer").equals("4350")) {
                System.out.println("Java SDK tests: switch timeout not set to 4350");
                return false;
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: switch timeout failed");
		    return false;
        }
        return true;
    }

    public boolean test_p2p_preference() {
		try {
		    // enabled
		    SessionProperties sp = new SessionProperties();
		    sp.p2p_preference = "enabled";
            OpenTokSession s = sdk.create_session("216.38.134.114", sp);
            TokBoxXML xml = get_session_info(s.session_id);
            if(!xml.getElementValue("preference", "p2p").equals("enabled")) {
                System.out.println("Java SDK tests: p2p not set to enabled");
                return false;
            }
		    // disable
		    sp = new SessionProperties();
		    sp.p2p_preference = "disabled";
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("preference", "p2p").equals("disabled")) {
                System.out.println("Java SDK tests: p2p not set to disabled");
                return false;
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: p2p preference failed");
		    return false;
        }
        return true;
    }

    public boolean test_echo_suppression() {
		try {
		    // enabled
		    SessionProperties sp = new SessionProperties();
		    sp.echoSuppression_enabled = true;
            OpenTokSession s = sdk.create_session("216.38.134.114", sp);
            TokBoxXML xml = get_session_info(s.session_id);
            if(!xml.getElementValue("enabled", "echoSuppression").equals("True")) {
                System.out.println("Java SDK tests: echoSuppression not set to true");
                return false;
            }
		    // disabled
		    sp = new SessionProperties();
		    sp.echoSuppression_enabled = false;
            s = sdk.create_session("216.38.134.114", sp);
            xml = get_session_info(s.session_id);
            if(!xml.getElementValue("enabled", "echoSuppression").equals("False")) {
                System.out.println("Java SDK tests: echoSuppression not set to true");
                return false;
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: echo suppression failed");
		    return false;
        }
        return true;
    }

    public boolean test_roles() {
		try {
            String s= sdk.create_session().session_id;

            // Default (Publisher)
            String t = sdk.generate_token(s);
            TokBoxXML xml = get_token_info(t);

            if(!xml.getElementValue("role", "token").trim().equals("publisher")) {
                System.out.println("Java SDK tests: default role not publisher");
                return false;
            }
            try {
                xml.getElementValue("subscribe", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: default role does not have subscriber permissions");
                return false;
            }
            try {
                xml.getElementValue("publish", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: default role does not have publiser permissions");
                return false;
            }
            try {
                xml.getElementValue("signal", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: default role does not have signaling permissions");
                return false;
            }
            try {
                xml.getElementValue("forceunpublish", "permissions");
                System.out.println("Java SDK tests: default role should not have force unpublish permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("forcedisconnect", "permissions");
                System.out.println("Java SDK tests: default role should not have force disconnect permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("record", "permissions");
                System.out.println("Java SDK tests: default role should not have record permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("playback", "permissions");
                System.out.println("Java SDK tests: default role should not have playback permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }

            // Publisher
            t = sdk.generate_token(s, RoleConstants.PUBLISHER);
            xml = get_token_info(t);

            if(!xml.getElementValue("role", "token").trim().equals("publisher")) {
                System.out.println("Java SDK tests: role not publisher");
                return false;
            }
            try {
                xml.getElementValue("subscribe", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: publisher role does not have subscriber permissions");
                return false;
            }
            try {
                xml.getElementValue("publish", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: publisher role does not have publiser permissions");
                return false;
            }
            try {
                xml.getElementValue("signal", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: publisher role does not have signaling permissions");
                return false;
            }
            try {
                xml.getElementValue("forceunpublish", "permissions");
                System.out.println("Java SDK tests: publisher role should not have force unpublish permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("forcedisconnect", "permissions");
                System.out.println("Java SDK tests: publisher role should not have force disconnect permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("record", "permissions");
                System.out.println("Java SDK tests: publisher role should not have record permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("playback", "permissions");
                System.out.println("Java SDK tests: publisher role should not have playback permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }

            // Subscriber
            t = sdk.generate_token(s, RoleConstants.SUBSCRIBER);
            xml = get_token_info(t);

            if(!xml.getElementValue("role", "token").trim().equals("subscriber")) {
                System.out.println("Java SDK tests: role not subscriber");
                return false;
            }
            try {
                xml.getElementValue("subscribe", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: subscriber role does not have subscriber permissions");
                return false;
            }
            try {
                xml.getElementValue("publish", "permissions");
                System.out.println("Java SDK tests: subscriber role should not have publiser permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("signal", "permissions");
                System.out.println("Java SDK tests: subscriber role should not have signaling permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("forceunpublish", "permissions");
                System.out.println("Java SDK tests: subscriber role should not have force unpublish permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("forcedisconnect", "permissions");
                System.out.println("Java SDK tests: subscriber role should not have force disconnect permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("record", "permissions");
                System.out.println("Java SDK tests: subscriber role should not have record permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }
            try {
                xml.getElementValue("playback", "permissions");
                System.out.println("Java SDK tests: subscriber role should not have playback permissions");
                return false;
            } catch (java.lang.NullPointerException e) {
                // expected
            }

            // Moderator
            t = sdk.generate_token(s, RoleConstants.MODERATOR);
            xml = get_token_info(t);

            if(!xml.getElementValue("role", "token").trim().equals("moderator")) {
                System.out.println("Java SDK tests: role not moderator");
                return false;
            }
            try {
                xml.getElementValue("subscribe", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have subscriber permissions");
                return false;
            }
            try {
                xml.getElementValue("publish", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have publiser permissions");
                return false;
            }
            try {
                xml.getElementValue("signal", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have signaling permissions");
                return false;
            }
            try {
                xml.getElementValue("forceunpublish", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have force unpublish permissions");
                return false;
            }
            try {
                xml.getElementValue("forcedisconnect", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have force disconnect permissions");
                return false;
            }
            try {
                xml.getElementValue("record", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have record permissions");
                return false;
            }
            try {
                xml.getElementValue("playback", "permissions");
            } catch (java.lang.NullPointerException e) {
                System.out.println("Java SDK tests: moderator role does not have playback permissions");
                return false;
            }


            // random input
            try {
                t = sdk.generate_token(s, "asdfasdf");
                System.out.println("Java SDK tests: nonvalid roles should not be accepted");
            } catch (OpenTokException e) {
                // Expected
            }

            try {
                t = sdk.generate_token(s, null);
                System.out.println("Java SDK tests: null role should not be accepted");
            } catch (OpenTokException e) {
                // Expected
            }

		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: roles failed");
		    return false;
        }
        return true;
    }

    public boolean test_expire_time() {
		try {
            String s= sdk.create_session().session_id;
            TokBoxXML xml;
            String t; // token

            // Past
            try {
                t = sdk.generate_token(s, RoleConstants.MODERATOR, new Date().getTime() - 100);
                System.out.println("Java SDK tests: expire time in the past should not be accepted");
                return false;
            } catch (OpenTokException e) {
                // Expected
            }
            // Now
            try {
                long time = new Date().getTime();
                t = sdk.generate_token(s, RoleConstants.MODERATOR, time);
                xml = get_token_info(t);

                if(!xml.getElementValue("expire_time", "token").trim().equals("" + time)) {
                    System.out.println("Java SDK tests: expire time not set to current time");
                    return false;
                }
            } catch (OpenTokException e) {
                System.out.println("Java SDK tests: current time not accepted as expire time");
                return false;
            }

            // Near future
            try {
                long time = new Date().getTime() + 34200;
                t = sdk.generate_token(s, RoleConstants.MODERATOR, time);
                xml = get_token_info(t);

                if(!xml.getElementValue("expire_time", "token").trim().equals("" + time)) {
                    System.out.println("Java SDK tests: expire time not properly set");
                    return false;
                }
            } catch (OpenTokException e) {
                System.out.println("Java SDK tests: expire time not accepted");
                return false;
            }

            // Far future
            try {
                t = sdk.generate_token(s, RoleConstants.MODERATOR, new Date().getTime() - 800000);
                System.out.println("Java SDK tests: expire time over 7 days away should not be accepted");
                return false;
            } catch (OpenTokException e) {
                // Expected
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: expire time failed");
		    return false;
        }
        return true;
    }

    public boolean test_connection_data() {
		try {
            String s= sdk.create_session().session_id;

            String test_string = "test string";
            String t = sdk.generate_token(s, RoleConstants.PUBLISHER, new Date().getTime(), test_string);
            TokBoxXML xml = get_token_info(t);
            if(!xml.getElementValue("connection_data", "token").trim().equals(test_string)) {
                System.out.println("Java SDK tests: connection data not set");
                return false;
            }


            test_string = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc" +
                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg" +
                "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh" +
                "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
                "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" +
                "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk" +
                "llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll" +
                "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" +
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo";
            try {
                t = sdk.generate_token(s, RoleConstants.PUBLISHER, new Date().getTime(), test_string);
                System.out.println("Java SDK tests: connection data over 1000 characters should not be accepted");
            } catch (OpenTokException e) {
                // expected
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: connection data failed");
		    return false;
        }
        return true;
    }
    public boolean test_old_session() {
		try {
            String s = "1abc70a34d069d2e6a1e565f3958b5250b435e32";
            String t = sdk.generate_token(s);
            TokBoxXML xml = get_token_info(t);

            if(!xml.getElementValue("role", "token").trim().equals("publisher")) {
                System.out.println("Java SDK tests: token with old session not properly created");
                return false;
            }
		} catch (OpenTokException e) {
		    System.out.println("Error: " + e.getMessage());
		    System.out.println("Java SDK tests: old session failed");
		    return false;
        }
        return true;
    }
	public String request(String reqString, Map<String, String> paramList, Map<String, String> headers){

		StringBuilder returnString = new StringBuilder();

		URL url = null;
		HttpURLConnection conn = null;
		BufferedReader br = null;
		OutputStreamWriter wr = null;
		BufferedWriter bufWriter = null;

		try {

			StringBuilder dataString = new StringBuilder();

			for(Iterator<String> i = paramList.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				String value = paramList.get(key);

				if(null != value) {
					value = URLEncoder.encode(paramList.get(key), "UTF-8").replaceAll("\\+", "%20");
					dataString.append(URLEncoder.encode(key, "UTF-8")).append("=").append(value).append("&");
				}
			}
			url = new URL(reqString);
			conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Content-Length", Integer.toString(dataString.toString().length()));
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("Accept", "text/html, application/xhtml+xml,application/xml");

			for(Iterator<String> i = headers.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				String value = headers.get(key);
				conn.setRequestProperty(key, value);
			}

			wr = new OutputStreamWriter(conn.getOutputStream(), "UTF8");
			bufWriter = new BufferedWriter( wr );
			bufWriter.write(dataString.toString());
			bufWriter.flush();

			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));

			String str;
			while(null != ((str = br.readLine())))
				{
					returnString.append(str);
					returnString.append("\n");
				}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != conn) {
					conn.disconnect();
				}

				if(null != wr) {
					wr.close();
				}

				if(null != bufWriter) {
					bufWriter.close();
				}

				if(null != br) {
					br.close();
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		return returnString.toString();
	}
}

class TestRunner {
	public static void main(String argv[]) throws OpenTokException {

	    UnitTest t = new UnitTest();

		int passed = 0;
		int failed = 0;
		int num; // needed for ternary operator?

        System.out.println("Running Java SDK tests");
		num = t.test_create_sesion() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_num_output_streams() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_switch_type() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_switch_timeout() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_p2p_preference() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_echo_suppression() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_roles() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_expire_time() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_connection_data() ? passed++ : failed++;
        System.out.print(".");
		num = t.test_old_session() ? passed++ : failed++;
        System.out.println(".");

		System.out.println("Java SDK tests completed\nPassed: " + passed + "\nFailed: " + failed);
	}
}

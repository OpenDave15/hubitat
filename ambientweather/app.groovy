/**
 * Import URL: https://raw.githubusercontent.com/mircolino/hubitat/master/ambientweather/app.groovy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 * Version History:
 *
 *   v1.0.05:
 *
 *   - added indoor/outdoor battery level reporting (100: OK, 0: Replace)
 *
 *   v1.0.00:
 *
 *   - removed double scheduling from original implementation
 *   - consolidated log into app
 *   - added 30 min auto-disable log
 *   - split device in two (indoor and outdoor sensor) to display appropriate temperature and humidity in two different tiles
 *
 * Credits:
 *
 *   Scott Grayban (https://gitlab.borgnet.us:8443/sgrayban/Hubitat-Ambient-Weather-Improved): improvements
 *   Howard Alden (https://github.com/thoward1234/Hubitat-Ambient-Weather): initial implementation
 *
*/

public static String version() { return "v1.0.05"; }

// ------------------------------------------------------------

definition(name: "AmbientWeather Station", namespace: "mircolino", author: "Mirco Caramori", description: "API to access ambientweather.net", iconUrl: "", iconX2Url: "");

// ------------------------------------------------------------

preferences {
  page(name: "page1", title: "Settings", nextPage: "page2", uninstall: true) {
    section {
      input(name: "apiKey", title: "API Key", type: "text", required: true);
      input(name: "applicationKey", title: "Application Key", type: "text", required: true);
      input(name: "debugOutput", type: "bool", title: "Enable debug logging", defaultValue: true);
    }
  }

  page(name: "page2");
  page(name: "page3");
}

// ------------------------------------------------------------

def page2() {
  def stations = [];
  def stationMacs = [];
  try {
    stations = getStations();
    stations.each { stationMacs << it.macAddress };
  }
  catch(groovyx.net.http.HttpResponseException e) {
    // Unauthorized
    return dynamicPage(name: "page2", title: "Error", nextPage: "page1", uninstall: true) {
      section {
        paragraph("Authorization error. Please try again.");
      }
    }
  }

  logDebug("Got stations: " + stations);

  return dynamicPage(name: "page2", title: "Select Station", nextPage: "page3", uninstall: true) {
    section {
      input(name: "station", title: "Station", type: "enum", options: stationMacs, required: true);
      input(name: "refreshInterval", title: "Refresh Interval (in minutes)", type: "number", range: "1..60", defaultValue: 5, required: true);
    }
  }
}

// ------------------------------------------------------------

def page3() {
  dynamicPage(name: "page3", title: "Confirm Settings", install: true, uninstall: true) {
    section {
      paragraph("Selected station: $station");
      paragraph("Refresh interval: $refreshInterval minute(s)");
    }

    section {
      paragraph("Press done to finish");
    }
  }
}

// ------------------------------------------------------------

def boolean isLogDebugOn() {
	return (settings.debugOutput || settings.debugOutput == null);
}

// ------------------------------------------------------------

def logDebugOff() {
  app.updateSetting("debugOutput",[value:"false",type:"bool"]);
}

// ------------------------------------------------------------

private logDebug(msg) {
	if (isLogDebugOn()) {
		log.debug("$msg");
	}
}

// Lifecycle functions ----------------------------------------

def installed() {
  logDebug("Installed");

  addChildDevice("mircolino", "AmbientWeather Outdoor Sensor", "$station-00", null, [completedSetup: true]);
  addChildDevice("mircolino", "AmbientWeather Indoor Sensor", "$station-01", null, [completedSetup: true]);

  initialize();
}

// ------------------------------------------------------------

def updated() {
  logDebug("Updated");

  unsubscribe();
  unschedule();
  initialize();
}

// ------------------------------------------------------------

def uninstalled() {
  logDebug("Uninstalled");

  deleteChildDevice("$station-01");
  deleteChildDevice("$station-00");
}

// ------------------------------------------------------------

def initialize() {
  // Turn off debug log in 30 minutes
  if (debugOutput) runIn(1800, logDebugOff);

  // Get the initial weather
  fetchNewWeather();

  // Schedule subsequent weather fetches every X minutes with Cron
  def m = refreshInterval;
  def h = Math.floor(m / 60);
  m -= h * 60;

  m = m == 0 ? "*" : "0/" + m.toInteger();
  h = h == 0 ? "*" : "0/" + h.toInteger();

  logDebug("Cron schedule with m: $m and h: $h");

  schedule("0 $m $h * * ? *", fetchNewWeather);
}

// Fetch functions --------------------------------------------

def getStations() throws groovyx.net.http.HttpResponseException {
  def data = [];

  requestData("/v1/devices", [applicationKey: applicationKey, apiKey: apiKey]) { response ->
    data = response.data;
  };

  return data;
}

// ------------------------------------------------------------

def getWeather() throws groovyx.net.http.HttpResponseException {
  def data = [];

  requestData("/v1/devices/$station", [applicationKey: applicationKey, apiKey: apiKey, limit: 1]) { response ->
    data = response.data;
  };

  return data[0];
}

// ------------------------------------------------------------

def requestData(path, query, code) {
  def params = [
    uri: "https://api.ambientweather.net/",
    path: path,
    query: query
  ];

  httpGet(params) { response ->
    code(response);
  };
}

// Loop -------------------------------------------------------

def fetchNewWeather() {
  def weather = getWeather();

  logDebug("Weather: "+weather);

  childDevices[0].setWeather(weather);
  childDevices[1].setWeather(weather);
}

// ------------------------------------------------------------

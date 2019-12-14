/**
 * IMPORT URL: https://raw.githubusercontent.com/mircolino/hubitat/master/ambientweather/app.groovy
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
 * Credits:
 *
 * Alden Howard (https://github.com/thoward1234/Hubitat-Ambient-Weather): initial implementation
 *
*/

definition(name: "AmbientWeather Station", namespace: "mircolino", author: "Mirco Caramori", description: "API to access ambientweather.net", iconUrl: "", iconX2Url: "");

// ------------------------------------------------------------

preferences {
  page(name: "page1", title: "Log In", nextPage: "page2", uninstall: true) {
    section {
      input(name: "applicationKey", title: "Application Key", type: "text", required: true);
      input(name: "apiKey", title: "API Key", type: "text", required: true);
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
        paragraph("There was an error authorizing you. Please try again.");
      }
    }
  }

  log.debug("Got stations: " + stations);

  return dynamicPage(name: "page2", title: "Select Station", nextPage: "page3", uninstall: true) {
    section {
      input(name: "station", title: "Station", type: "enum", options: stationMacs, required: true);
      input(name: "refreshInterval", title: "Refresh Interval (in minutes)", type: "number", range: "1..3600", defaultValue: 1, required: true);
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

// Lifecycle functions ----------------------------------------

def installed() {
  log.debug("Installed");

  addDevices();
  initialize();
  runEvery5Minutes(fetchNewWeather);
}

// ------------------------------------------------------------

def updated() {
  log.debug("Updated");

  unsubscribe();
  unschedule();
  installed();
  initialize();
}

// ------------------------------------------------------------

def initialize() {
  fetchNewWeather();

  // Chron schedule, refreshInterval is int
  def m = refreshInterval;
  def h = Math.floor(m / 60);
  m -= h * 60;

  m = m == 0 ? "*" : "0/" + m.toInteger();
  h = h == 0 ? "*" : "0/" + h.toInteger();

  log.debug("CRON schedule with m: $m and h: $h");

  schedule("0 $m $h * * ? *", fetchNewWeather);
}

// Children ---------------------------------------------------

def addDevices() {
  addChildDevice("mircolino", "AmbientWeather Outdoor Sensor", "$station-00", null, [completedSetup: true]);
  addChildDevice("mircolino", "AmbientWeather Indoor Sensor", "$station-01", null, [completedSetup: true]);
}

// fetch functions --------------------------------------------

def getStations() throws groovyx.net.http.HttpResponseException {
  def data = [];

  def params = [
    uri: "https://api.ambientweather.net/",
    path: "/v1/devices",
    query: [applicationKey: applicationKey, apiKey: apiKey]
  ];

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

// loop -------------------------------------------------------

def fetchNewWeather() {
  def weather = getWeather();

  //log.debug("Weather: " + weather);

  childDevices[0].setWeather(weather);
  childDevices[1].setWeather(weather);
}

// ------------------------------------------------------------

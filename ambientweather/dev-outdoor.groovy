/**
 * IMPORT URL: https://raw.githubusercontent.com/mircolino/hubitat/master/ambientweather/dev-outdoor.groovy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 * Credits:
 *
 * Scott Grayban (https://gitlab.borgnet.us:8443/sgrayban/Hubitat-Ambient-Weather-Improved): improvements
 * Alden Howard (https://github.com/thoward1234/Hubitat-Ambient-Weather): initial implementation
 *
*/

metadata {
  definition(name: "Ambient Weather Outdoor Sensor", namespace: "mircolino", author: "Mirco Caramori") {
    capability "Temperature Measurement"
    capability "Relative Humidity Measurement"
    capability "Pressure Measurement"
    capability "Illuminance Measurement"
    capability "Refresh"
    capability "Sensor"
    capability "Actuator"

    // Current Conditions
    attribute "weather", "string"
    attribute "weatherIcon", "string"
    attribute "dewPoint", "number"
    attribute "comfort", "number"
    attribute "feelsLike", "number"

    // Precipitation
    attribute "precip_today", "number"
    attribute "precip_1hr", "number"
    attribute "hourlyrainin", "number"
    attribute "weeklyrainin", "number"
    attribute "monthlyrainin", "number"
    attribute "totalrainin", "number"
    attribute "lastRain", "string"

    // Wind
    attribute "wind", "number"
    attribute "wind_gust", "number"
    attribute "maxdailygust", "number"
    attribute "wind_degree", "number"
    attribute "wind_dir", "string"
    attribute "wind_direction", "string"

    // Light
    attribute "solarradiation", "number"
    attribute "uv", "number"
  }

  preferences {
    section("Preferences") {
      input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    }
  }
}

// ------------------------------------------------------------

private logger(type, msg) {
  if (type && msg && settings?.showLogs) {
    log."${type}" "${msg}"
  }
}

// ------------------------------------------------------------

def refresh() {
  parent.fetchNewWeather();
}

// ------------------------------------------------------------

def setWeather(weather) {
  logger("debug", "Weather: "+weather);

  // Set Temperature
  sendEvent(name: "temperature", value: weather.tempf, unit: '°F', isStateChange: true);

  // Set Humidity
  sendEvent(name: "humidity", value: weather.humidity, unit: '%', isStateChange: true);

  // Set DewPoint
  sendEvent(name: "dewPoint", value: weather.dewPoint, unit:'°F', isStateChange: true);

  // Set Comfort Level
  float temp = 0.0;

  temp = (weather.dewPoint - 35);
  if (temp <= 0) {
    temp = 0.0;
  }
  else if (temp >= 40.0) {
    temp = 100.0;
  }
  else {
    temp = (temp/40.0)*100.0;
  }
  temp = temp.round(1);
  sendEvent(name: "comfort", value: temp, isStateChange: true);

  // Set Barometric Pressure
  sendEvent(name: "pressure", value: weather.baromrelin, unit: 'in', isStateChange: true);

  // Set Feels Like Temperature
  sendEvent(name: "feelsLike", value: weather.feelsLike, unit: '°F', isStateChange: true);

    // Rain
  sendEvent(name: "precip_today", value: weather.dailyrainin, unit: 'in', isStateChange: true);
  sendEvent(name: "precip_1hr", value: weather.hourlyrainin, unit: 'in', isStateChange: true);
  sendEvent(name: "weeklyrainin", value: weather.weeklyrainin, unit: 'in', isStateChange: true);
  sendEvent(name: "monthlyrainin", value: weather.monthlyrainin, unit: 'in', isStateChange: true);
  sendEvent(name: "totalrainin", value: weather.totalrainin, unit: 'in', isStateChange: true);
  sendEvent(name: "hourlyrainin", value: weather.hourlyrainin, unit: 'in', isStateChange: true);
  sendEvent(name: "lastRain",  value: weather.lastRain, isStateChange: true);

  // Wind
  sendEvent(name: "wind", value: weather.windspeedmph, unit: 'mph', isStateChange: true);
  sendEvent(name: "wind_gust", value: weather.windgustmph, unit: 'mph', isStateChange: true);
  sendEvent(name: "maxdailygust", value: weather.maxdailygust, unit: 'mph', isStateChange: true);
  sendEvent(name: "wind_degree", value: weather.winddir, unit: '°', isStateChange: true);

  temp = weather.winddir
  if (temp < 22.5) {
    sendEvent(name:  "wind_direction", value: "North", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "N", isStateChange: true);
  }
  else if (temp < 67.5) {
    sendEvent(name:  "wind_direction", value: "Northeast", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "NE", isStateChange: true);
  }
  else if (temp < 112.5) {
    sendEvent(name: "wind_direction", value: "East", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "E", isStateChange: true);
  }
  else if (temp < 157.5) {
    sendEvent(name: "wind_direction", value: "Southeast", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "SE", isStateChange: true);
  }
  else if (temp < 202.5) {
    sendEvent(name: "wind_direction", value: "South", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "S", isStateChange: true);
  }
  else if (temp < 247.5) {
    sendEvent(name: "wind_direction", value: "Southwest", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "SW", isStateChange: true);
  }
  else if (temp < 292.5) {
    sendEvent(name: "wind_direction", value: "West", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "W", isStateChange: true);
  }
  else if (temp < 337.5) {
    sendEvent(name: "wind_direction", value: "Northwest", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "NW", isStateChange: true);
  }
  else {
    sendEvent(name:  "wind_direction", value: "North", isStateChange: true);
    sendEvent(name:  "wind_dir", value: "N", isStateChange: true);
  }

  // UV and Light
  sendEvent(name: "solarradiation", value: weather.solarradiation, isStateChange: true);
  sendEvent(name: "illuminance", value: weather.solarradiation, isStateChange: true);
  sendEvent(name: "uv", value: weather.uv, isStateChange: true);
}

// ------------------------------------------------------------

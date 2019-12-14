/**
 * IMPORT URL: https://raw.githubusercontent.com/mircolino/hubitat/master/ambientweather/dev-indoor.groovy
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
 * Howard Alden (https://github.com/thoward1234/Hubitat-Ambient-Weather): initial implementation
 *
*/

metadata {
  definition(name: "AmbientWeather Indoor Sensor", namespace: "mircolino", author: "Mirco Caramori") {
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

    // Indoor Conditions
    attribute "pressure_indoor", "number"

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

  float temp = 0.0;

  // Set Temperature
  sendEvent(name: "temperature", value: weather.tempinf, unit: '°F', isStateChange: true);

  // Set Humidity
  sendEvent(name: "humidity", value: weather.humidityin, unit: '%', isStateChange: true);

  // Set DewPoint
  sendEvent(name: "dewPoint", value: weather.dewPoint, unit:'°F', isStateChange: true);

  // Set Comfort Level
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

  // Set Indoor Barometric Pressure
  sendEvent(name: "pressure_indoor", value: weather.baromabsin, unit: 'in', isStateChange: true);

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

  String wind_direction = null;
  String wind_dir = null;

  if (weather.winddir) {
    temp = weather.winddir % 360;
    if (temp >= 337.5 || temp < 22.5) {
      wind_direction = "North";
      wind_dir = "N";
    }
    else if (temp < 67.5) {
      wind_direction = "Northeast";
      wind_dir = "NE";
    }
    else if (temp < 112.5) {
      wind_direction = "East";
      wind_dir = "E";
    }
    else if (temp < 157.5) {
      wind_direction = "Southeast";
      wind_dir = "SE";
    }
    else if (temp < 202.5) {
      wind_direction = "South";
      wind_dir = "S";
    }
    else if (temp < 247.5) {
      wind_direction = "Southwest";
      wind_dir = "SW";
    }
    else if (temp < 292.5) {
      wind_direction = "West";
      wind_dir = "W";
    }
    else {
      wind_direction = "Northwest";
      wind_dir = "NW";
    }
  }

  sendEvent(name:  "wind_direction", value: wind_direction, isStateChange: true);
  sendEvent(name:  "wind_dir", value: wind_dir, isStateChange: true);

  // UV and Light
  sendEvent(name: "illuminance", value: weather.solarradiation, isStateChange: true);
  sendEvent(name: "uv", value: weather.uv, isStateChange: true);
}

// ------------------------------------------------------------

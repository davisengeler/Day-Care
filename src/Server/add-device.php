<?php

  include("config.php");
  include("default-functions.php");

  // Connect to DB. Comes from default-functions.php
  db_connect();

  $deviceID = $_GET["deviceID"];

  mysql_query("INSERT INTO Devices(DeviceID) VALUES ($deviceID)");

?>

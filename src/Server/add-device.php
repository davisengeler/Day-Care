<?php

  include("default-functions.php");
  include("config.php");

  // Connect to DB. Comes from default-functions.php
  db_connect();

  if(isset($_GET["deviceID"]))
  {
    $deviceID = $_GET["deviceID"];
    if ($result = mysql_query("INSERT INTO Devices(DeviceID) VALUES ('$deviceID');"))
    {
      echo "You have requested authentication for the device: " . $deviceID;
    }
    else
    {
      echo "Couldn't request authentication: " . mysql_error();
    }

  }

?>

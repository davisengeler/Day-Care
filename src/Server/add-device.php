<?php

  include("config.php");
  include("default-functions.php");

  // Connect to DB. Comes from default-functions.php
  db_connect();

  // TODO: change _GET to _SET
  if(isset($_GET["deviceID"]))
  {
    // Gets the device ID from the request.
    $deviceID = $_GET["deviceID"];

    if ($result = mysql_query("INSERT INTO Devices(DeviceID) VALUES ('$deviceID');"))
    {
      // New Request Submitted
      echo "You have requested authentication for the device: " . $deviceID;
    }
    else
    {
      // New Request Denied
      echo "Couldn't request authentication: " . mysql_error();
    }

  }

?>

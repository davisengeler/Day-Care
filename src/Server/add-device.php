<?php

  include("config.php");
  include("default-functions.php");

  // Connect to DB. Comes from default-functions.php
  db_connect();

  // TODO: change _GET to _POST
  if(isset($_GET["deviceID"]))
  {

    // Gets the device ID from the request.
    $deviceID = $_GET["deviceID"];

    if ($result = mysql_query("INSERT INTO Devices(DeviceID) VALUES ('$deviceID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "This device has requested authentication from an administrator.",
        "deviceID" => $deviceID
        );

      echo json_encode($response);


    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "This device did not request an authentication. It may already be pending.",
        "deviceID" => $deviceID
        );

      echo json_encode($response);
    }

  }

?>

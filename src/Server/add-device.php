<?php

  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME);

  // TODO: change _GET to _POST
  if(isset($_GET["deviceID"]))
  {

    // Gets the device ID from the request.
    $deviceID = $_GET["deviceID"];

    if (mysqli_query($database, "CALL add_device('$deviceID');"))
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
        "statusMessage" => "This device did not request an authentication. It may already be pending." . $mysqlierror,
        "deviceID" => $deviceID
        );

      echo json_encode($response);
    }

  }

?>

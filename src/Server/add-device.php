<?php

  include("user.php");
  $database = connectDB();

  // Make sure the call to the API was authorized
  // The if statement looks weird, but it makes sense. There are two instances in which
  //   the account may not be authorized to make calls. If the apikey/apipass does point
  //   to an account, it must make sure it's verified. It should also fail if the
  //   apikey/apipass is just totally wrong.
  $authorized = getAccount($database, "api", array($_GET['apikey'], $_GET['apipass']));
  mysqli_next_result($database);
  if (gettype($authorized) == "array" && $authorized['successful'] == false)
  {
    // The account was not authorized because the api key/pass was wrong
    echo json_encode(generateResult(false, "You are not authorized to proceed. The API Key and API Pass combination was not correct."));
  }
  else if (gettype($authorized) == "object" && $authorized->verified != 1)
  {
    // The account was not authorized because the it hasn't been verified by an admin.
    echo json_encode(generateResult(false, "Can not proceed. Your account has not been verified."));
  }
  else
  {
    // Everything is good and the user can proceed.

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


  }

?>

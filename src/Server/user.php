<?php

include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // What is the request for?
  if (isset($_GET['getaccounttypes']))
  {
    $accountTypes = array();
    $result = mysqli_query($database, "CALL get_account_types();");

    while($row = mysqli_fetch_array($result))
    {
      $accountTypes[$row['AccID']] = $row['Title'];
    }
    echo json_encode($accountTypes);
  }
  else if (isset($_GET['add']))
  {
    // Gets the account information from the request.
    $ssn = $_GET["ssn"];
    $firstName = $_GET["firstname"];
    $lastName = $_GET["lastname"];
    $address = $_GET["address"];
    $phone = $_GET["phone"];
    $email = $_GET["email"];
    $pass = $_GET["pass"];
    $accID = $_GET["accid"];

    if (mysqli_query($database, "CALL add_new_account('$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "This account has requested authentication from an administrator.",
        "deviceID" => $deviceID
        );

      echo json_encode($response);


    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "This account did not request an authentication. It may already be pending." . $mysqlierror,
        "deviceID" => $deviceID
        );

      echo json_encode($response);
    }
  }
  else if (isset($_GET['edit']))
  {
    // Gets the account information from the request.
    $ssn = $_GET["ssn"];
    $firstName = $_GET["firstname"];
    $lastName = $_GET["lastname"];
    $address = $_GET["address"];
    $phone = $_GET["phone"];
    $email = $_GET["email"];
    $pass = $_GET["pass"];
    $accID = $_GET["accid"];

    if (mysqli_query($database, "CALL edit_account('$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "The account information has been updated.",
        "deviceID" => $deviceID
        );

      echo json_encode($response);
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "The account information was not changed. " . $mysqlierror,
        "deviceID" => $deviceID
        );

      echo json_encode($response);
    }
  }

?>

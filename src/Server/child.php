<?php
  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // What is the request for?
  if (isset($_GET['add']))
  {
    // Gets the child information from the request.
    $ssn = $_GET["ssn"];
    $firstName = $_GET["firstname"];
    $lastName = $_GET["lastname"];
    $dob = $_GET["dob"];
    $parentID = $_GET["parentid"];
    $classID = $_GET["classid"];

    if (mysqli_query($database, "CALL add_new_child($ssn, '$firstName', '$lastName', $dob, $parentID, $classID);"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "The child has been added to the parent account."
        );

      echo json_encode($response);
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "Something was wrong with this request to add a child. " . $mysqlierror
        );

      echo json_encode($response);
    }
  }
?>

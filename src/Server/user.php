<?php

  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // What is the request for?
  // Get Account Types
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
  // Add New Account
  else if (isset($_GET['add']))
  {
    // Gets the account information from the request.
    $ssn = $_GET["ssn"];
    $firstName = $_GET["firstname"];
    $lastName = $_GET["lastname"];
    $address = $_GET["address"];
    $phone = $_GET["phone"];
    $email = $_GET["email"];
    $pass = md5($_GET["pass"]);
    $accID = $_GET["accid"];

    if (mysqli_query($database, "CALL add_new_account('$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "This account has requested authentication from an administrator."
        );

      echo json_encode($response);
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "This account did not request an authentication. It may already be pending. " . mysqli_error($database)
        );

      echo json_encode($response);
    }
  }
  // Edit Account
  else if (isset($_GET['edit']))
  {
    // Gets the account information from the request.
    $userID = $_GET["userid"];
    $ssn = $_GET["ssn"];
    $firstName = $_GET["firstname"];
    $lastName = $_GET["lastname"];
    $address = $_GET["address"];
    $phone = $_GET["phone"];
    $email = $_GET["email"];
    $pass = $_GET["pass"];
    $accID = $_GET["accid"];

    if (mysqli_query($database, "CALL edit_account('$userID', '$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "The account information has been updated."
        );

      echo json_encode($response);
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "The account information was not changed. " . mysqli_error($database)
        );

      echo json_encode($response);
    }
  }
  // Log In
  else if (isset($_GET['login']))
  {
    // Gets the account information from the request.
    $email = $_GET["email"];
    $pass = md5($_GET["pass"]);

    $accountInfo = array();
    if ($result = mysqli_query($database, "CALL get_account('$email', '$pass');"))
    {
      $row = mysqli_fetch_array($result);
      $accountInfo["UserID"] = $row["UserID"];
      $accountInfo["FirstName"] = $row["FirstName"];
      $accountInfo["LastName"] = $row["LastName"];
      $accountInfo["Address"] = $row["Address"];
      $accountInfo["Phone"] = $row["Phone"];
      $accountInfo["Email"] = $row["Email"];
      $accountInfo["AccID"] = $row["AccID"];
      $accountInfo["Verified"] = $row["Verified"];

      // Gathers list of children
      $childIDs = array();
      $children = mysqli_query($database, "CALL get_children('$accountInfo["UserID"]');")
      if (mysqli_num_rows($children))
      {
        while ($row = mysqli_fetch_array($children))
        {
          $childIDs[] = $row["ChildID"];
        }
        $accountInfo["ChildIDs"] = $childIDs;
      }

      if ($accountInfo["UserID"] != null)
      {
        echo json_encode($accountInfo);
      }
      else
      {
        echo json_encode(generateResult(false, "The username and password combo was incorrect."));
      }
    }
    else
    {
      echo json_encode(generateResult(false, "There was an issue with the database. " . mysqli_error($database)));
    }
  }
  // Setting Approval
  else if (isset($_GET['setapproval']))
  {
    // Accepts "approve" or "deny"

    $userID = $_GET['userid'];
    $decision = $_GET['decision'];

    if ($result = mysqli_query($database, "CALL " . $decision . "_account('$userID');"))
    {
      echo json_encode(generateResult(true, "The request to " . $decision . " the account was successful."));
    }
    else
    {
      echo json_encode(generateResult(false, "There was an issue with the " . $decision . " request. " . mysqli_error($database)));
    }
  }

  function generateResult($successful, $message)
  {
    $response = array(
      "successful" => $successful,
      "statusMessage" => $message
      );

    return $response;
  }

?>

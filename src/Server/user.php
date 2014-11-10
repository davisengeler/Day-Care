<?php

  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // Class for creating User objects
  class User
  {
    public $userID, $firstName, $lastName, $ssn, $address, $phone, $email, $accID, $verified;
    public $children = array();

    // function __construct()
    // {
    //   // Allows to create an empty instance
    // }
    //
    // function __construct($userID, $firstName, $lastName, $ssn, $address, $phone, $email, $accID, $verified)
    // {
    //   $this->userID = $userID;
    //   $this->firstName = $firstName;
    //   $this->lastName = $lastName;
    //   $this->ssn = $ssn;
    //   $this->address = $address;
    //   $this->phone = $phone;
    //   $this->email = $email;
    //   $this->accID = $accID;
    //   $this->verified = $verified;
    // }
  }

  // Returns list of the account type IDs and their meanings
  function getAccountTypes($database)
  {
    $accountTypes = array();
    $result = mysqli_query($database, "CALL get_account_types();");

    while($row = mysqli_fetch_array($result))
    {
      $accountTypes[$row['AccID']] = $row['Title'];
    }


    return $accountTypes;
  }

  // Adds a new Account
  function addAccount($database, $ssn, $firstName, $lastName, $address, $phone, $email, $pass, $accID)
  {
    if (mysqli_query($database, "CALL add_new_account('$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "This account has requested authentication from an administrator."
        );

      return $response;
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "This account did not request an authentication. It may already be pending. " . mysqli_error($database)
        );

      return $response;
    }
  }

  // Logs in an Account
  function logIn($database, $email, $pass)
  {
    if ($result = mysqli_query($database, "CALL get_account('$email', '$pass');"))
    {
      $row = mysqli_fetch_array($result);
      $accountInfo = new User;
      $accountInfo->userID = $row["UserID"];
      $accountInfo->ssn = $row["SSN"];
      $accountInfo->firstName = $row["FirstName"];
      $accountInfo->lastName = $row["LastName"];
      $accountInfo->address = $row["Address"];
      $accountInfo->phone = $row["Phone"];
      $accountInfo->email = $row["Email"];
      $accountInfo->accID = $row["AccID"];
      $accountInfo->verified = $row["Verified"];

      mysqli_next_result($database);

      $accountInfo->children = getChildren($database, $accountInfo->userID);

      if ($accountInfo->userID != null)
      {
        return $accountInfo;
      }
      else
      {
        return generateResult(false, "The username and password combo was incorrect.");
      }
    }
    else
    {
      return generateResult(false, "There was an issue with the database. " . mysqli_error($database));
    }
  }

  // Gets list of ChildIDs for a given ParentID
  function getChildren($database, $parentID)
  {
    $childIDs = array();

    if ($query = mysqli_query($database, "CALL get_children($parentID);"))
    {
      while ($row = mysqli_fetch_array($query))
      {
        $childIDs[] = $row["ChildID"];
      }
      return $childIDs;
    }
    else
    {
      return mysqli_error($database);
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





  // ===================================================


  // What is the request for?
  // Get Account Types
  if (isset($_GET['getaccounttypes']))
  {
    echo json_encode(getAccountTypes($database));
  }
  // Add New Account
  else if (isset($_GET['add']))
  {
    echo json_encode(addAccount($database, $_GET["ssn"], $_GET["firstname"], $_GET["lastname"], $_GET["address"], $_GET["phone"], $_GET["email"], $md5($_GET["pass"]), $_GET["accid"]));
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
    echo json_encode(login($database, $_GET["email"], md5($_GET["pass"])));
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

  function getAccountInfo($email, $password)
  {

  }

?>

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

  // Returns list of IDs of pending account approvals.
  function getPendingAccounts($database)
  {
    $pendingAccounts = array();
    $result = mysqli_query($database, "CALL get_pending_accounts();");

    while($row = mysqli_fetch_array($result))
    {
      // Creates the pending user
      $singleAccount = new User;
      $singleAccount->userID = $row["UserID"];
      $singleAccount->ssn = $row["SSN"];
      $singleAccount->firstName = $row["FirstName"];
      $singleAccount->lastName = $row["LastName"];
      $singleAccount->address = $row["Address"];
      $singleAccount->phone = $row["Phone"];
      $singleAccount->email = $row["Email"];
      $singleAccount->accID = $row["AccID"];
      $singleAccount->verified = $row["Verified"];

      // Adds that user to the array
      $pendingAccounts[] = $singleAccount;
    }

    return $pendingAccounts;
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
  // Returns a User object in an array wrapper
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

      // Frees up mysqli for another request
      mysqli_next_result($database);

      // Gets the children
      $accountInfo->children = getChildren($database, $accountInfo->userID);
      if ($accountInfo->userID != null)
      {
        return array($accountInfo);
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
      if (count($childIDs) == 0) $childIDs = null;
      return $childIDs;
    }
    else
    {
      return mysqli_error($database);
    }
  }

  // Updates an account's information
  function updateAccount($database, $userID, $ssn, $firstName, $lastName, $address, $phone, $email, $pass, $accID)
  {
    if (mysqli_query($database, "CALL edit_account('$userID', '$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID');"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "The account information has been updated."
        );

      return $response;
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "The account information was not changed. " . mysqli_error($database)
        );

      return $response;
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
  else if (isset($_GET['getpendingaccounts']))
  {
    echo json_encode(getPendingAccounts($database));
  }
  // Add New Account
  else if (isset($_GET['add']))
  {
    $apiResponse = addAccount(
      $database,
      $_GET["ssn"],
      $_GET["firstname"],
      $_GET["lastname"],
      $_GET["address"],
      $_GET["phone"],
      $_GET["email"],
      $md5($_GET["pass"]),
      $_GET["accid"]);

    echo json_encode($apiResponse);
  }
  // Edit Account
  else if (isset($_GET['edit']))
  {
    $apiResponse = updateAccount(
      $database,
      $_GET["userid"],
      $_GET["ssn"],
      $_GET["firstname"],
      $_GET["lastname"],
      $_GET["address"],
      $_GET["phone"],
      $_GET["email"],
      $_GET["pass"],
      $_GET["userid"]);

    echo json_encode($apiResponse);
  }
  // Log In
  else if (isset($_GET['login']))
  {
    $apiResponse = login($database, $_GET["email"], md5($_GET["pass"]));
    echo json_encode($apiResponse);
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

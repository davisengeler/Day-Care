<?php

  include("config.php");
  $database = connectDB();

  // Class for creating User objects
  class User
  {
    public $userID, $firstName, $lastName, $ssn, $address, $phone, $email, $accID, $verified, $apiKey, $apiPass;
    public $children = array();
  }

  // Generates an API key and pass for an account
  function generateAPIKeyPass($database, $ssn, $firstName)
  {
    $apiKey = md5($ssn + time());
    $apiPass = md5($firstName + time());

    return array($apiKey, $apiPass);
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
      $singleAccount->apiKey = $row["APIKey"];
      $singleAccount->apiPass = $row["APIPass"];

      // Adds that user to the array
      $pendingAccounts[] = $singleAccount;
    }

    return $pendingAccounts;
  }

  // Adds a new Account
  function addAccount($database, $ssn, $firstName, $lastName, $address, $phone, $email, $pass, $accID)
  {
    // Generates API validation info
    $apiValidation = generateAPIKeyPass($database, $ssn, $firstName);
    $apiKey = $apiValidation[0];
    $apiPass = $apiValidation[1];

    if (mysqli_query($database, "CALL add_new_account('$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID', '$apiKey', '$apiPass');"))
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

  // Sets Account verification status
  function setApproval($database, $userID, $decision)
  {
    if ($result = mysqli_query($database, "CALL " . $decision . "_account('$userID');"))
    {
      return generateResult(true, "The request to " . $decision . " the account was successful.");
    }
    else
    {
      return generateResult(false, "There was an issue with the " . $decision . " request. " . mysqli_error($database));
    }
  }

  // Returns all teachers in an array of User objects
  function getTeacherList($database)
  {
    if ($result = mysqli_query($database, "CALL get_teacher_list()"))
    {
      $teacherList = array();
      while ($row = mysqli_fetch_array($result))
      {
        mysqli_next_result($database);
        $teacherList[] = getAccount($database, "ssn", array($row["SSN"]));
      }
      return $teacherList;
    }
    else
    {
      return generateResult(false, "Database error: " . mysqli_error($database));
    }
  }

  // Returns a User object
  function getAccount($database, $type, $params)
  {
    switch ($type)
    {
      case "ssn":
        $ssn = $params[0];
        $databaseCall = "CALL get_account_by_ssn($ssn);";
        break;
      case "userID":
        $accID = $params[0];
        $databaseCall = "CALL get_account_by_userid($accID)";
        break;
      case "login":
        $email = $params[0];
        $pass = $params[1];
        $databaseCall = "CALL get_account('$email', '$pass');";
        break;
    }

    if ($result = mysqli_query($database, $databaseCall))
    {
      $row = mysqli_fetch_array($result);
      $account = new User;
      $account->userID = $row["UserID"];
      $account->ssn = $row["SSN"];
      $account->firstName = $row["FirstName"];
      $account->lastName = $row["LastName"];
      $account->address = $row["Address"];
      $account->phone = $row["Phone"];
      $account->email = $row["Email"];
      $account->accID = $row["AccID"];
      $account->verified = $row["Verified"];
      $account->apiKey = $row["APIKey"];
      $account->apiPass = $row["APIPass"];

      // Frees up mysqli for another request
      mysqli_next_result($database);

      // Gets the children
      $account->children = getChildren($database, $account);

      // Checks to see if the user is valid
      if ($account->userID != null)
      {
        return $account;
      }
      else
      {
        return generateResult(false, "No accounts match that information.");
      }
    }
    else
    {
      return generateResult(false, "There was an issue with the database. " . mysqli_error($database));
    }
  }

  // Gets list of ChildIDs for a given ParentID
  function getChildren($database, $account)
  {
    $childIDs = array();

    switch ($account->accID)
    {
      case 2:
        // User is a teacher
        $databaseCall = "CALL get_teacher_students";
        break;
      default:
        // User is a parent
        $databaseCall = "CALL get_children";
        break;
    }

    if ($query = mysqli_query($database, $databaseCall . "($account->userID);"))
    {
      while ($row = mysqli_fetch_array($query))
      {
        $childIDs[] = $row["ChildID"];
      }
      if (count($childIDs) == 0) $childIDs = array();
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
      md5($_GET["pass"]),
      $_GET["accid"]);

    echo json_encode($apiResponse);
  }
  // Edit Account
  else if (isset($_GET['edit']))
  {
    // If the password has been changed, encrypt and save it. Otherwise, use the currently encrypted password for the account.
    $pass = ""; // for scope
    if (!isset($_GET['pass'] == "")
    {
      $pass = md5($_GET['pass']);
    }
    else
    {
      $user = getAccount($database, "ssn", $_GET['ssn']);
      $pass = $user->pass; // will already be encrypted
    }
    
    $apiResponse = updateAccount(
      $database,
      $_GET["userid"],
      $_GET["ssn"],
      $_GET["firstname"],
      $_GET["lastname"],
      $_GET["address"],
      $_GET["phone"],
      $_GET["email"],
      $pass,
      $_GET["accid"]);

    echo json_encode($apiResponse);
  }
  // Log In
  else if (isset($_GET['login']))
  {
    $type = "login";
    $params = array($_GET["email"], md5($_GET["pass"]));
    // needs to be an array for android...
    $apiResponse = array(getAccount($database, $type, $params));
    echo json_encode($apiResponse);
  }
  // Get Account by SSN
  else if (isset($_GET['getaccountbyssn']))
  {
    $type = "ssn";
    $params = array($_GET["ssn"]);
    // needs to be an array for android...
    $apiResponse = array(getAccount($database, $type, $params));
    echo json_encode($apiResponse);
  }
  // Setting Approval
  else if (isset($_GET['setapproval']))
  {
    // Accepts "approve" or "deny"
    $apiResponse = setApproval($database, $_GET['userid'], $_GET['decision']);
    echo json_encode($apiResponse);
  }
  // Get List of Teachers
  else if (isset($_GET['teacherlist']))
  {
    $apiResponse = getTeacherList($database);
    echo json_encode($apiResponse);
  }

?>

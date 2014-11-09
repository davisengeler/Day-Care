<?php
  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // What is the request for?
  // Add New Child
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
      echo json_encode($generateResult(true, "The child has been added to the parent account."));
    }
    else
    {
      // New Request Denied
      echo json_encode(generateResult(false, "Something was wrong with this request to add a child. " . mysqli_error($database)));
    }
  }
  // Gather the info about a child
  else if (isset($_GET['getinfo']))
  {
    $childID = $_GET['childid'];
    if ($result = mysqli_query($database, "CALL get_child_info($childID);"))
    {
      $row = mysqli_fetch_array($result);
      $childInfo["SSN"] = $row["SSN"];
      $childInfo["FirstName"] = $row["FirstName"];
      $childInfo["LastName"] = $row["LastName"];
      $childInfo["DOB"] = $row["DOB"];
      $childInfo["ParentID"] = $row["ParentID"];
      $childInfo["ClassID"] = $row["ClassID"];

      if ($childInfo["SSN"] != null)
      {
        echo json_encode($childInfo);
      }
      else
      {
        echo json_encode(generateResult(false, "There was an issue with the request for child information. Make sure you have the correct ChildID."));
      }
    }
    else
    {
      echo json_encode(generateResult(false, "There was a database error for the request to get the child information. " . mysqli_error($database)));
    }
  }
  else if (isset($_GET['setclass']))
  {
    $childID = $_GET['childid'];
    $classID = $_GET['classid'];

    if ($result = mysqli_query($database, "CALL change_child_class($childID, $classID);"))
    {
      echo json_encode(generateResult(true, "The child's classroom was changed successfully."));
    }
    else
    {
      echo json_encode(generateResult(false, "There was an error changing that child's classroom: " . mysqli_error($database)));
    }
  }
  // Get Child Notes
  // TODO: Something weird with the loop when it gets multiple child IDs.
  else if (isset($_GET['getnotes']))
  {
    $childIDs = json_decode($_GET['childids']);
    $noteList = array();

    // TODO: Echoing the errors as the loops goes could give an overall invalid JSON string, therefore not being decodable.
    foreach ($childIDs as $currentChild)
    {
      if ($result = mysqli_query($database, "CALL get_notes($currentChild);"))
      {
        // Gathers all the notes for the child
        while($row = mysqli_fetch_array($result))
        {
          $note = array();
          $note["ChildID"] = $currentChild;
          $note["NoteID"] = $row["NoteID"];
          $note["Message"] = $row["Message"];
          $note["SubjectID"] = $row["SubjectID"];
          $note["NoteType"] = $row["NoteType"];

          if ($note["NoteID"] != null)
          {
            $noteList[] = $note;
          }
        }
      }
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

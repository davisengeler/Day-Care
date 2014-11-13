<?php
  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // Adds a child
  function addChild($database, $ssn, $firstName, $lastName, $dob, $parentID, $classID)
  {
    if (mysqli_query($database, "CALL add_new_child($ssn, '$firstName', '$lastName', $dob, $parentID, $classID);"))
    {
      // New Request Submitted
      return $generateResult(true, "The child has been added to the parent account.");
    }
    else
    {
      // New Request Denied
      return generateResult(false, "Something was wrong with this request to add a child. " . mysqli_error($database));
    }
  }

  // Gets info on a child
  function getChild($database, $type, $parameter)
  {
    switch ($type)
    {
      case "childID":
        $childID = $parameter;
        $databaseCall = "CALL get_child_info($childID);";
        break;
      case "ssn":
        $ssn = $parameter;
        $databaseCall = "CALL get_child_info_by_ssn($ssn);";
        break;
    }
    if ($result = mysqli_query($database, $databaseCall))
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
        return $childInfo;
      }
      else
      {
        return generateResult(false, "There was an issue with the request for child information. Make sure you have the correct ChildID.");
      }
    }
    else
    {
      return generateResult(false, "There was a database error for the request to get the child information. " . mysqli_error($database));
    }
  }





  // =============================================




  // What is the request for?
  // Add New Child
  if (isset($_GET['add']))
  {
    $apiResponse = addChild(
      $database,
      $_GET["ssn"],
      $_GET["firstname"],
      $_GET["lastname"],
      $_GET["dob"],
      $_GET["parentid"],
      $_GET["classid"]);
    echo json_encode($apiResponse);
  }
  // Gather the info about a child
  else if (isset($_GET['getinfo']))
  {
    $childID = $_GET['childid'];

  }
  else if (isset($_GET['setclass']))
  {
    $childID = $_GET['childid'];
    $teacherID = $_GET['teacherid'];

    if ($result = mysqli_query($database, "CALL change_child_class($childID, $teacherID);"))
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

    // if ($result = mysqli_query($database, "CALL get_notes($childIDs);"))
    // {
    //   // Gathers all the notes for the child
    //   while($row = mysqli_fetch_array($result))
    //   {
    //     $note = array();
    //     $note["ChildID"] = $childIDs;
    //     $note["NoteID"] = $row["NoteID"];
    //     $note["Message"] = $row["Message"];
    //     $note["SubjectID"] = $row["SubjectID"];
    //     $note["NoteType"] = $row["NoteType"];
    //
    //     if ($note["NoteID"] != null)
    //     {
    //       $noteList[] = $note;
    //     }
    //   }
    //   echo json_encode($noteList);
    // }
    // else
    // {
    //   echo "PROBLEM";
    // }

    //TODO: Echoing the errors as the loops goes could give an overall invalid JSON string, therefore not being decodable.
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
      mysqli_next_result($database);
    }
    echo json_encode($noteList);
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

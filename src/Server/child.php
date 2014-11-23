<?php

  include("config.php");
  $database = connectDB();

  class Child
  {
    public $childID, $ssn, $firstName, $lastName, $dob, $parentID, $classID, $attendID;
  }

  // The standard formatting for a general API call's result
  function generateResult($successful, $message)
  {
    $response = array(
      "successful" => $successful,
      "statusMessage" => $message
    );

    return $response;
  }

  // Adds a child
  function addChild($database, $ssn, $firstName, $lastName, $dob, $parentID, $teacherID)
  {
    // Gets the ClassID for the given teacher
    $classID = getTeacherClass($database, $teacherID);

    mysqli_next_result($database);

    echo "CALL add_new_child($ssn, '$firstName', '$lastName', '$dob', $parentID, $classID);";

    if (mysqli_query($database, "CALL add_new_child($ssn, '$firstName', '$lastName', '$dob', $parentID, $classID);"))
    {
      // New Request Submitted
      return generateResult(true, "The child has been added to the parent account.");
    }
    else
    {
      // New Request Denied
      return generateResult(false, "Something was wrong with this request to add a child. " . mysqli_error($database));
    }
  }

  // Returns the ClassID for a given teacher
  function getTeacherClass($database, $teacherID)
  {
    if ($result = mysqli_query($database, "CALL get_teacher_class($teacherID)"))
    {
      if (mysqli_num_rows($result) > 0)
      {
        $row = mysqli_fetch_array($result);
        return $row["ClassID"];
      }
      else
      {
        return null;
      }
    }
    else
    {
      return null;
    }
  }

  // Edits a child
  function editChild($database, $childID, $ssn, $firstName, $lastName, $dob, $parentID, $classID)
  {
    if (mysqli_query($database, "CALL edit_child($childID, $ssn, '$firstName', '$lastName', '$dob', $parentID, $classID);"))
    {
      return generateResult(true, "The child information has been updated.");
    }
    else
    {
      return generateResult(false, "There was an error updating the child information. " . mysqli_error($database));
    }
  }

  // Adds a note for an array of children
  function addNote($database, $message, $noteType, $subjectID, $childrenArray)
  {
    // Sets up a status message string
    $statuses = "";
    $allSuccessful = true;

    // Prepares the note
    if($result = mysqli_query($database, "CALL prepare_note('$message', $subjectID, $noteType);"))
    {
      $note = mysqli_fetch_array($result);
      $noteID = $note["LAST_INSERT_ID()"];
      foreach ($childrenArray as $currentChild)
      {
        mysqli_next_result($database);
        if($result = mysqli_query($database, "CALL link_note($noteID, $currentChild);"))
        {
          $statuses = $statuses . "Note added to ChildID " . $currentChild . " successfully. ";
        }
        else
        {
          $statuses = $statuses . "Note failed to be added to ChildID " . $currentChild . "(" . mysqli_error($database) . "). ";
          $allSuccessful = false;
        }
      }
    }
    else
    {
      $allSuccessful = false;
      $statuses = "Note failed to be added. " . mysqli_error($database);
    }
    return generateResult($allSuccessful, $statuses);
  }

  // Gets info on a child
  function getChild($database, $type, $parameter)
  {
    switch ($type)
    {
      case "childids":
        $databaseCall = "CALL get_child_info";
        break;
      case "ssn":
        $databaseCall = "CALL get_child_info_by_ssn";
        break;
    }

    $infoArray = json_decode($parameter, true);
    $childArray = array();

    foreach ($infoArray as $currentInfo)
    {
      if ($result = mysqli_query($database, $databaseCall . "(" . $currentInfo . ");"))
      {
        $child = new Child;
        $row = mysqli_fetch_array($result);
        $child->childID = $row["ChildID"];
        $child->ssn = $row["SSN"];
        $child->firstName = $row["FirstName"];
        $child->lastName = $row["LastName"];
        $child->dob = $row["DOB"];
        $child->parentID = $row["ParentID"];
        $child->classID = $row["ClassID"];
        $child->attendID = getAttendID($database, $child->childID);

        if ($child->ssn != null)
        {
          $childArray[] = $child;
        }
      }
      else
      {

      }

      mysqli_next_result($database);
    }

    return $childArray;
  }

  // Gets all notes for an array of children
  function getNotes($database, $childIDs)
  {
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
    return $noteList;
  }

  // Sets a child's current classroom assignment
  function setClass($database, $childID, $teacherID)
  {
    if ($result = mysqli_query($database, "CALL change_child_class($childID, $teacherID);"))
    {
      return generateResult(true, "The child's classroom was changed successfully.");
    }
    else
    {
      return generateResult(false, "There was an error changing that child's classroom: " . mysqli_error($database));
    }
  }

  // Signs in an array of ChildIDs
  function signIn($database, $childIDs, $time)
  {
    // Prepares the attendance entry
    foreach ($childIDs as $childID)
    {
      $attendID = 0;
      if($result = mysqli_query($database, "CALL prepare_attendance($time);"))
      {
        $attendance = mysqli_fetch_array($result);
        $attendID = $attendance["LAST_INSERT_ID()"];
      }
      else
      {
        return generateResult(false, "There was an error creating an attendance entry for ChildID " . $childID . ". " . mysqli_error($database));
      }

      mysqli_next_result($database);

      // Links the child to the new attendance entry
      if($result = mysqli_query($database, "CALL link_attendance($attendID, $childID);"))
      {
        // do nothing until the loop is finished
      }
      else
      {
        return generateResult(false, "Failure to link the child to the attendance entry for ChildID " . $childID . ". " . mysqli_error($database));
      }

      mysqli_next_result($database);
    }

    return generateResult(true, "The array of children have been signed in.");
  }

  // Signs out an array of ChildIDs
  function signOut($database, $attendIDs, $time)
  {
    foreach ($attendIDs as $attendID)
    {
      if($result = mysqli_query($database, "CALL sign_out($attendID, $time);"))
      {
        // do nothing until the end of the loop
      }
      else
      {
        return generateResult(false, "Failure to sign out the ChildID " . $childID . ". " . mysqli_error($database));
      }
    }
    return generateResult(true, "The array of children are signed out.");
  }

  // Checks if a ChildID is signed in or not
  function getAttendID($database, $childID)
  {
    mysqli_next_result($database);
    if ($result = mysqli_query($database, "CALL get_child_attend_id($childID);"))
    {
      if (mysqli_num_rows($result) == 0)
      {
        return null;
      }
      else
      {
        $row = mysqli_fetch_array($result);
        if ($row["DepTime"] == null)
        {
          return $row['AttendID'];
        }
      }
    }
    else
    {
      return null;
    }
  }


  // ================================================================================


  // Allows the outside API work with the server

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
      $_GET["teacherid"]);
    echo json_encode($apiResponse);
  }

  if (isset($_GET['edit']))
  {
    $apiResponse = editChild(
    $database,
    $_GET["childid"],
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
    if ($_GET['ssn'])
    {
      $type = "ssn";
    }
    elseif ($_GET['childids'])
    {
      $type = "childids";
    }

    $apiResponse = getChild($database, $type, $_GET[$type]);
    echo json_encode($apiResponse);
  }

  // Add a note for an array of ChildIDs
  else if (isset($_GET['addnote']))
  {
    $apiResponse = addNote($database, $_GET['message'], $_GET['notetype'], $_GET['subjectid'], json_decode($_GET['children'], true));
    echo json_encode($apiResponse);
  }

  // Change a child's class
  else if (isset($_GET['setclass']))
  {
    $apiResponse = setClass($database, $_GET['childid'], $_GET['teacherid']);
    echo json_encode($apiResponse);
  }

  // Get Child Notes
  else if (isset($_GET['getnotes']))
  {
    $childIDs = json_decode($_GET['childids']);
    $apiResponse = getNotes($database, $childIDs);
    echo json_encode($apiResponse);
  }

  // Signs in an array of ChildIDs
  else if (isset($_GET['signin']))
  {
    $childIDs = json_decode($_GET['childids']);
    $apiResponse = signIn($database, $childIDs, time());
    echo json_encode($apiResponse);
  }

  // Signs out an array of ChildIDs
  else if (isset($_GET['signout']))
  {
    $attendIDs = json_decode($_GET['attendids']);
    $apiResponse = signOut($database, $attendIDs, time());
    echo json_encode($apiResponse);
  }
?>

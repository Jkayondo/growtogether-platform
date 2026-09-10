import { getMyActiveTeachingAssignments } from "./teachingAssignmentApi";
import {
  activateTeachingAssignment,
  cancelTeachingAssignment,
  completeTeachingAssignment,
  createTeachingAssignment,
  getTeachingAssignmentById,
  getTeachingAssignmentsByStatus,
  requestTeachingAssignmentApproval,
  suspendTeachingAssignment
} from "./teachingAssignmentApi";

import type {
  CreateTeachingAssignmentRequest,
  TeachingAssignment
} from "../types/teachingAssignment";


export async function loadTeachingAssignmentsByStatus(
  assignmentStatus: string
): Promise<TeachingAssignment[]> {

  const response =
    await getTeachingAssignmentsByStatus(
      assignmentStatus
    );

  return response.data;

}


export async function loadTeachingAssignmentById(
  assignmentId: string
): Promise<TeachingAssignment> {

  const response =
    await getTeachingAssignmentById(
      assignmentId
    );

  return response.data;

}


export async function saveTeachingAssignment(
  request: CreateTeachingAssignmentRequest
): Promise<TeachingAssignment> {

  const response =
    await createTeachingAssignment(
      request
    );

  return response.data;

}


export async function submitTeachingAssignmentForApproval(
  assignmentId: string
): Promise<TeachingAssignment> {

  const response =
    await requestTeachingAssignmentApproval(
      assignmentId
    );

  return response.data;

}


export async function enableTeachingAssignment(
  assignmentId: string
): Promise<TeachingAssignment> {

  const response =
    await activateTeachingAssignment(
      assignmentId
    );

  return response.data;

}


export async function suspendAssignment(
  assignmentId: string
): Promise<TeachingAssignment> {

  const response =
    await suspendTeachingAssignment(
      assignmentId
    );

  return response.data;

}


export async function finishTeachingAssignment(
  assignmentId: string
): Promise<TeachingAssignment> {

  const response =
    await completeTeachingAssignment(
      assignmentId
    );

  return response.data;

}


export async function cancelAssignment(
  assignmentId: string
): Promise<TeachingAssignment> {

  const response =
    await cancelTeachingAssignment(
      assignmentId
    );

  return response.data;

}


export async function loadMyActiveTeachingAssignments()
  : Promise<TeachingAssignment[]> {
  const response = await getMyActiveTeachingAssignments();
  if (!response.success || !Array.isArray(response.data)) {
    throw new Error("Invalid teaching assignment response.");
  }
  return response.data;
}

import apiClient from "./apiClient";


export interface VisitorCheckInRequest {

    visitorName: string;

    purpose: string;

}



export interface VisitorCheckInResponse {

    id: string;

    visitorName: string;

    purpose: string;

    status: string;

}



export interface VisitorRequest {

    id: string;

    visitorName: string;

    purpose: string;

    status: string;

}



export interface VisitorCheckIn {

    id: string;

    visitorName: string;

    purpose: string;

    status: string;

}



export interface VisitorBadge {

    id: string;

    badgeNumber: string;

    visitorName: string;

    badgeStatus: string;

    badgeType?: string;

}




export async function checkInVisitor(
    request: VisitorCheckInRequest
): Promise<VisitorCheckInResponse> {


    return apiClient.post(
        "/api/v1/visitor-check-ins",
        request
    );

}



export async function getVisitorRequests()
    : Promise<VisitorRequest[]> {


    return apiClient.get(
        "/api/v1/visitor-requests"
    );

}



export async function getVisitorCheckIns()
    : Promise<VisitorCheckIn[]> {


    return apiClient.get(
        "/api/v1/visitor-check-ins"
    );

}



export async function getVisitorBadges()
    : Promise<VisitorBadge[]> {


    return apiClient.get(
        "/api/v1/visitor-badges"
    );

}
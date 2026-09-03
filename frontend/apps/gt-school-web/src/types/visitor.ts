export interface VisitorBadge {

    id: string;

    visitorCheckInId: string;

    badgeNumber: string;

    badgeType?: string;

    badgeStatus: string;

    issuedAt: string;

    issuedBy: string;

    returnedAt?: string;

    returnedBy?: string;

}


export interface VisitorRequest {

    id: string;

    visitorName: string;

    purpose: string;

    status: string;

}


export interface VisitorCheckIn {

    id: string;

    visitorId: string;

    gateLocation?: string;

    badgeNumber?: string;

    status: string;

    checkedInAt: string;

    checkedOutAt?: string;

}

import {
    getVisitorRequests,
    getVisitorCheckIns,
    getVisitorBadges
} from "./visitorApi";



export async function getVisitorSummary() {


    const requests =
        await getVisitorRequests();


    const checkIns =
        await getVisitorCheckIns();


    const badges =
        await getVisitorBadges();



    return {


        totalRequests:
            requests.length,


        activeVisitors:
            checkIns.filter(
                (item) =>
                    item.status === "ACTIVE"
            ).length,


        activeBadges:
            badges.filter(
                (badge) =>
                    badge.badgeStatus === "ISSUED"
            ).length,


        pendingRequests:
            requests.filter(
                (request) =>
                    request.status === "PENDING"
            ).length,


        unreturnedBadges:
            badges.filter(
                (badge) =>
                    badge.badgeStatus !== "RETURNED"
            ).length


    };

}

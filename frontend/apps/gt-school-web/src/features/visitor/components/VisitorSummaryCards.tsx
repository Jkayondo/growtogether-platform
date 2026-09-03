import VisitorIcon from "../../../assets/icons/visitor.svg?react";

import GTStatCard from "../../../components/common/GTStatCard";


interface VisitorSummaryCardsProps {

    totalRequests: number;

    activeVisitors: number;

    activeBadges: number;

}


export default function VisitorSummaryCards({

    totalRequests,

    activeVisitors,

    activeBadges

}: VisitorSummaryCardsProps) {


    return (

        <div className="gt-dashboard-summary-grid">


            <GTStatCard

                icon={VisitorIcon}

                title="Visitor Requests"

                value={totalRequests.toLocaleString()}

            />


            <GTStatCard

                icon={VisitorIcon}

                title="Visitors Inside"

                value={activeVisitors.toLocaleString()}

            />


            <GTStatCard

                icon={VisitorIcon}

                title="Active Badges"

                value={activeBadges.toLocaleString()}

            />


        </div>

    );

}

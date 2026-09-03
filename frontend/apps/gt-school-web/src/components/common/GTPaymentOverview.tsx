import type { PaymentSummary } from "../../types/dashboard";
import GTPieChart from "../charts/GTPieChart";


interface Props {
  payment: PaymentSummary;
}


export default function GTPaymentOverview({
  payment
}: Props) {


  const paymentData = [
    {
      name: "Paid",
      value: payment.amountPaid,
      color: "#8FC73E",
    },
    {
      name: "Pending",
      value: payment.amountPending,
      color: "#D9D9D9",
    },
  ];


  return (

    <div className="gt-payment-overview">

      <h3>
        Term Payment Overview
      </h3>


      <div className="gt-payment-layout">


        <div className="gt-payment-chart">

          <GTPieChart
            data={paymentData}
            centerText={`${payment.collectionRate}%`}
          />


          <div className="gt-payment-legend">

            <div>
              <span className="gt-dot paid"></span>
              Paid
            </div>


            <div>
              <span className="gt-dot pending"></span>
              Pending
            </div>

          </div>


        </div>



        <div className="gt-payment-summary">


          <div className="gt-payment-item">

            <span>
              Amount Paid:
            </span>

            <strong>
              UGX {payment.amountPaid.toLocaleString()}
            </strong>

          </div>



          <div className="gt-payment-item">

            <span>
              Amount Pending:
            </span>

            <strong>
              UGX {payment.amountPending.toLocaleString()}
            </strong>

          </div>

        </div>

      </div>


    </div>

  );
}

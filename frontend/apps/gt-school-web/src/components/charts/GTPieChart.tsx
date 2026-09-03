import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

interface GTPieChartProps {
  data: {
    name: string;
    value: number;
    color: string;
  }[];

  centerText?: string;
}


export default function GTPieChart({
  data,
  centerText,
}: GTPieChartProps) {

  return (

    <ResponsiveContainer width="100%" height={180}>

      <PieChart>


        <Pie
          data={data}
          dataKey="value"
          nameKey="name"
          innerRadius={55}
          outerRadius={65}
          paddingAngle={2}
          cx="50%"
          cy="50%"
        >

          {data.map((entry, index) => (

            <Cell
              key={`cell-${index}`}
              fill={entry.color}
            />

          ))}

        </Pie>


        <text
          x="50%"
          y="48%"
          textAnchor="middle"
          dominantBaseline="middle"
          fontSize="28"
          fontWeight="700"
          fill="#1f2937"
        >

          {centerText}

        </text>


        <text
          x="50%"
          y="62%"
          textAnchor="middle"
          dominantBaseline="middle"
          fontSize="13"
          fill="#6b7280"
        >

          Rate

        </text>


        <Tooltip />

      </PieChart>

    </ResponsiveContainer>

  );
}

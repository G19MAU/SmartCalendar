import {Box, Paper, Typography} from "@mui/material";
import {useCalendarContext} from "../../../../context/CalendarContext";
import {alpha} from "@mui/material/styles";
import dayjs from "dayjs";

const toMinutes = (timeStr) => {
    if (!timeStr) return Number.MAX_SAFE_INTEGER;
    const [h, m] = timeStr.split(":").map(Number);
    return h * 60 + m;
};

const MonthlyActivityBox = ({ filteredActivities, onClick }) => {
    const { categories } = useCalendarContext();

    if (!filteredActivities || filteredActivities.length === 0 ) return null;



    return (
        <Box sx={{ display:"flex", flexDirection:"column", gap:0.5 }}>
            {filteredActivities.sort((a, b) => toMinutes(a.startTime) - toMinutes(b.startTime)).map((activity, i, arr) => {
                if (i > 2) return null; {/* This and following, the arr above included, added as an experiment */}
                if (i === 2 && arr.length > 3) {
                    const hiddenCount = arr.length - 2;
                    return (
                        <Paper
                            key="more"
                            elevation={1}
                            sx={{
                                p: 0.5,
                                backgroundColor: "grey.200",
                                cursor: "pointer",
                                zIndex: 1,
                                textAlign: "center",
                            }}
                        >
                            +{hiddenCount}
                        </Paper>
                    );
                }
                const category = categories?.find(cat => cat.id === activity.categoryId);
                const tempBackgroundColor = category ? category.color : "#013e87";
                const alphaTempBackgroundColor = alpha(tempBackgroundColor, 0.3);
                const combinedDateTime = new Date(`${activity.date}T${activity.endTime}`);
                const past = combinedDateTime.getTime() < Date.now();
                const backgroundColor = past ? "grey.200" : alphaTempBackgroundColor;

                return (
                    <Paper
                        key={i}
                        elevation={1}
                        sx={{
                            p: 0.5,
                            borderLeft:"5px solid",
                            borderLeftColor: tempBackgroundColor,
                            backgroundColor,
                            cursor: "pointer",
                            color: "black",
                            zIndex: 1,
                            "&:hover": {
                                backgroundColor: alpha(tempBackgroundColor, 0.6), // 60% opacity
                            },
                        }}
                        onClick={e => {
                            e.stopPropagation();
                            onClick(e, activity);
                        }}

                    >
                        <Typography variant="subtitle2">
                            {activity.name}
                        </Typography>
                    </Paper>
                )
            })}
        </Box>
    );
};

export default MonthlyActivityBox;
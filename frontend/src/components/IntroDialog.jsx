import React, {useState} from "react";
import {
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Button,
    Box,
} from "@mui/material";
import { useNavigate } from "react-router-dom";

const steps = [
    {
        title: "Välkommen till SmartCalendar!",
        heading: "Titta på denna video för att förstå din dagliga agenda 🙂",
        video: "/videos/DagensAgenda.mp4",
    },
    {
        title: "Välkommen till SmartCalendar!",
        heading: "Titta på denna video för att förstå din kalender 📅",
        video: "/videos/Kalender.mp4",
    },
    {
        title: "Välkommen till SmartCalendar!",
        heading: "Titta på denna video för att förstå din att-göra-lista ✅",
        video: "/videos/ToDoList.mp4",
    },
    {
        title: "Välkommen till SmartCalendar!",
        heading: "Titta på denna video för att förstå dina inställningar ⚙️",
        video: "/videos/Inställningar.mp4",
    },
];

function IntroDialog({ open, onClose }) {
    const [step, setStep] = useState(0);
    const navigate = useNavigate();

    const handleNext = () => {
        if (step < steps.length - 1) {
            setStep(step + 1);
        } else {
            onClose();
            navigate("/calendar");
        }
    };

    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="md"
        >
            <DialogTitle>
                <Box sx={{display: "flex", alignItems: "center", justifyContent: "space-between", width: "100%"}}>
                    {steps[step].title}
                    <Button onClick={onClose} variant="contained">Hoppa över</Button>
                </Box>
            </DialogTitle>
            <DialogContent dividers>
                <h3 style={{ textAlign: "center" }}>{steps[step].heading}</h3>
                <video
                    key={steps[step].video}
                    width="100%"
                    controls
                >
                    <source src={steps[step].video} type="video/mp4" />
                    Din webbläsare stöder inte videouppspelning.
                </video>
            </DialogContent>
            <DialogActions>
                <Box sx={{ display: "flex", justifyContent: "space-between", width: "100%" }}>
                    <Button onClick={() => setStep(Math.max(0, step - 1))} variant="contained" disabled={step === 0}>
                        Föregående
                    </Button>
                    <Button onClick={handleNext} variant="contained">
                        {step < steps.length - 1 ? "Nästa" : "Till kalendern"}
                    </Button>
                </Box>
            </DialogActions>
        </Dialog>
    );
}

export default IntroDialog;

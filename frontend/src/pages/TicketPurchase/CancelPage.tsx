import { Button, Grid, Typography } from '@mui/material';
import React, { useEffect, useState } from 'react';
import { NavbarLoggedIn } from '../../Navbar';
import { Navigate, useParams } from 'react-router-dom';

export const CancelPage: React.FC = (): JSX.Element => {

  // Set parameters
  const { orderId } = useParams<{ orderId: string }>();

  const token = window.localStorage.getItem('accessToken');
  const email: any = window.localStorage.getItem('email');
  const ticketMetadataJSON: any = window.localStorage.getItem('ticketSetMetadata');
  const ticketsetMetadata = JSON.parse(ticketMetadataJSON);

  useEffect(() => {
    cancelOrder(orderId);
    //calling backend to remove facial record
    if (ticketsetMetadata != null) {
      ticketsetMetadata.forEach((metadata: any, index: any) => {
        removeFacialRecords(metadata.eventId, metadata.label);
      });
      leaveQueue(ticketsetMetadata[0].eventId);
    }
  }, []);

  //call backend to cancel order
  const cancelOrder = async (orderId: any) => {
    // Calling backend API
    fetch(`${process.env.REACT_APP_BACKEND_URL}/order/cancel/${orderId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      method: 'PUT',
    })
      .catch((err) => {
        window.alert(err);
      });
  };

  //call backend to cancel order
  const leaveQueue = async (eventId: any) => {
    const formData = new FormData();
    formData.append('eventId', eventId);
    // Calling backend API
    fetch(`${process.env.REACT_APP_BACKEND_URL}/event/leave-queue`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      method: 'PUT',
      body: formData,
    })
      .catch((err) => {
        window.alert(err);
      });
  };

  //call backend to delete facial record
  const removeFacialRecords = async (eventId: any, label: any) => {
    const formData = new FormData();
    formData.append('email', email);
    formData.append('eventId', eventId);
    formData.append('label', label);

    // Calling backend API
    fetch(`${process.env.REACT_APP_FACIAL_URL}/face/facial-deletion`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      method: 'DELETE',
      body: formData,
    })
      .catch((err) => {
        window.alert(err);
      });
  };

  return (
    <>
      {
        token != null ?
          <Navigate to={`/cancel/${orderId}`} /> : <Navigate to="/Forbidden" />
      }
      <NavbarLoggedIn />
      <Grid style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <Grid style={{ background: '#F8F8F8', height: '300px', width: '650px', borderRadius: '8px', marginTop:75, display: 'flex', flexDirection:'column', alignItems: 'center'}}>
          <Typography style={{ font: 'roboto', fontWeight: 500, fontSize: '26px', marginTop:75}}>
            Order Cancelled
          </Typography>
          <Typography style={{ font: 'roboto', fontWeight: 400, fontSize: '16px', marginTop:5 }}>
            Your ticket purchase did not go through. 
          </Typography>
          <Typography style={{ font: 'roboto', fontWeight: 400, fontSize: '16px', }}>
            Please return to the event page to purchase the tickets.
          </Typography>
          <Button variant="outlined" href={`/Event`}
                sx={{
                    border: '1px solid #FF5C35',
                    borderRadius: '8px',
                    color: '#FF5C35',
                    height: 39.5,
                    width: 295,
                    marginTop: 2,
                    ":hover": {
                        bgcolor: "#FF5C35",
                        color: 'white',
                        BorderColor: '#FF5C35'
                    }
                }}>
            Return to Event
          </Button>
        </Grid>
      </Grid>
      {/* <Grid
        container
        sx={{
          justifyContent: "center", alignItems: "center", position: 'relative',
          mt: '25%',
          mb: '25%',
          ml: '35%',
          mr: '35%',
          minHeight: '700px',
          minWidth: '400px',
          maxHeight: '800px',
          maxWidth: '600px',
        }}
        style={{ minHeight: '200px', border: '2px solid red', padding: '20px' }}
      >
        <Grid item>
          <Typography variant="h4" gutterBottom>
            Order Canceled
          </Typography>
          <Typography variant="body1" paragraph>
            Your order has been canceled.
          </Typography>
          <Button variant="contained" color="primary" href={`/Event`}>
            Return to Event
          </Button>
        </Grid>
      </Grid> */}
    </>
  )
}
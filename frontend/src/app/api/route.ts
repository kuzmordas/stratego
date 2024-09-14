const executeCommand = async (command: string, data: any) => {
  console.log(process.env.BACKEND_URL)
  const response = await fetch(
    `${process.env.BACKEND_URL}/${command}`,
    {method: "POST", body: JSON.stringify(data)});
  return {
    status: response.status,
    data: await response.json()
  }
}

export async function POST(request: Request) {
  const body = await request.json();
  if (body.command) {
    const {data, status} = await executeCommand(body.command, body.data);
    return Response.json(data, {status});
  }
 
  return Response.json({})
}

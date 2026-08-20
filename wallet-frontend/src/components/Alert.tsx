interface Props {
  message: string | null
}

export function ErrorAlert({ message }: Props) {
  if (!message) return null
  return (
    <div className="alert alert-error" role="alert">
      {message}
    </div>
  )
}

export function SuccessAlert({ message }: { message: string | null }) {
  if (!message) return null
  return (
    <div className="alert alert-success" role="status">
      {message}
    </div>
  )
}
